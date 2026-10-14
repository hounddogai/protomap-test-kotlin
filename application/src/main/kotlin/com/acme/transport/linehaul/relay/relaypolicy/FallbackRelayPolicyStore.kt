package com.acme.transport.linehaul.relay.relaypolicy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback relay policies by code. */
interface FallbackRelayPolicyStore {
    fun find(code: String): FallbackRelayPolicy?
    fun save(item: FallbackRelayPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRelayPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRelayPolicyStore(
    var items: MutableMap<String, FallbackRelayPolicy>,
    var order: MutableList<String>,
) : FallbackRelayPolicyStore {
    override fun find(code: String): FallbackRelayPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRelayPolicy) {
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

    override fun list(): MutableList<FallbackRelayPolicy> {
        val out = mutableListOf<FallbackRelayPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRelayPolicyStore(): FallbackRelayPolicyStore {
    return MemoryFallbackRelayPolicyStore(mutableMapOf<String, FallbackRelayPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRelayPolicyStore(
    var inner: FallbackRelayPolicyStore,
    var cache: TtlCache<FallbackRelayPolicy>,
    var hits: Int,
    var misses: Int,
) : FallbackRelayPolicyStore {
    override fun find(code: String): FallbackRelayPolicy? {
        val hit = (cache.get(code) as FallbackRelayPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRelayPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRelayPolicy> = inner.list()
}

fun newCachedFallbackRelayPolicyStore(inner: FallbackRelayPolicyStore, ttl: Int, clock: Clock): FallbackRelayPolicyStore {
    return CachedFallbackRelayPolicyStore(
        inner = inner,
        cache = newTtlCache<FallbackRelayPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback relay policies. Empty fields match all. */
data class FallbackRelayPolicyQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRelayPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRelayPolicy>): MutableList<FallbackRelayPolicy> {
        val matched = filter(items) { candidate: FallbackRelayPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRelayPolicies(store: FallbackRelayPolicyStore, query: FallbackRelayPolicyQuery): MutableList<FallbackRelayPolicy> {
    return query.apply(store.list())
}
