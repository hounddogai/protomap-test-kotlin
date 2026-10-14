package com.acme.transport.linehaul.relay.relaygroup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered relay policies by code. */
interface TieredRelayPolicyStore {
    fun find(code: String): TieredRelayPolicy?
    fun save(item: TieredRelayPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRelayPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRelayPolicyStore(
    var items: MutableMap<String, TieredRelayPolicy>,
    var order: MutableList<String>,
) : TieredRelayPolicyStore {
    override fun find(code: String): TieredRelayPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRelayPolicy) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<TieredRelayPolicy> {
        val out = mutableListOf<TieredRelayPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRelayPolicyStore(): TieredRelayPolicyStore {
    return MemoryTieredRelayPolicyStore(mutableMapOf<String, TieredRelayPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRelayPolicyStore(
    var inner: TieredRelayPolicyStore,
    var cache: TtlCache<TieredRelayPolicy>,
    var hits: Int,
    var misses: Int,
) : TieredRelayPolicyStore {
    override fun find(code: String): TieredRelayPolicy? {
        val hit = (cache.get(code) as TieredRelayPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRelayPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRelayPolicy> = inner.list()
}

fun newCachedTieredRelayPolicyStore(inner: TieredRelayPolicyStore, ttl: Int, clock: Clock): TieredRelayPolicyStore {
    return CachedTieredRelayPolicyStore(
        inner = inner,
        cache = newTtlCache<TieredRelayPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered relay policies. Empty fields match all. */
data class TieredRelayPolicyQuery(
    var lane: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRelayPolicy): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRelayPolicy>): MutableList<TieredRelayPolicy> {
        val matched = filter(items) { candidate: TieredRelayPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRelayPolicies(store: TieredRelayPolicyStore, query: TieredRelayPolicyQuery): MutableList<TieredRelayPolicy> {
    return query.apply(store.list())
}
