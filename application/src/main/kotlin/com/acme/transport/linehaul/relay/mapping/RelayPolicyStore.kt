package com.acme.transport.linehaul.relay.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay policies by code. */
interface RelayPolicyStore {
    fun find(code: String): RelayPolicy?
    fun save(item: RelayPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayPolicyStore(
    var items: MutableMap<String, RelayPolicy>,
    var order: MutableList<String>,
) : RelayPolicyStore {
    override fun find(code: String): RelayPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayPolicy) {
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

    override fun list(): MutableList<RelayPolicy> {
        val out = mutableListOf<RelayPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayPolicyStore(): RelayPolicyStore {
    return MemoryRelayPolicyStore(mutableMapOf<String, RelayPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayPolicyStore(
    var inner: RelayPolicyStore,
    var cache: TtlCache<RelayPolicy>,
    var hits: Int,
    var misses: Int,
) : RelayPolicyStore {
    override fun find(code: String): RelayPolicy? {
        val hit = (cache.get(code) as RelayPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayPolicy> = inner.list()
}

fun newCachedRelayPolicyStore(inner: RelayPolicyStore, ttl: Int, clock: Clock): RelayPolicyStore {
    return CachedRelayPolicyStore(
        inner = inner,
        cache = newTtlCache<RelayPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay policies. Empty fields match all. */
data class RelayPolicyQuery(
    var locale: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayPolicy): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayPolicy>): MutableList<RelayPolicy> {
        val matched = filter(items) { candidate: RelayPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayPolicies(store: RelayPolicyStore, query: RelayPolicyQuery): MutableList<RelayPolicy> {
    return query.apply(store.list())
}
