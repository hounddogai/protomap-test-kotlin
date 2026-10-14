package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive utilization policies by code. */
interface AdaptiveUtilizationPolicyStore {
    fun find(code: String): AdaptiveUtilizationPolicy?
    fun save(item: AdaptiveUtilizationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveUtilizationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveUtilizationPolicyStore(
    var items: MutableMap<String, AdaptiveUtilizationPolicy>,
    var order: MutableList<String>,
) : AdaptiveUtilizationPolicyStore {
    override fun find(code: String): AdaptiveUtilizationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveUtilizationPolicy) {
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

    override fun list(): MutableList<AdaptiveUtilizationPolicy> {
        val out = mutableListOf<AdaptiveUtilizationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveUtilizationPolicyStore(): AdaptiveUtilizationPolicyStore {
    return MemoryAdaptiveUtilizationPolicyStore(
        mutableMapOf<String, AdaptiveUtilizationPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveUtilizationPolicyStore(
    var inner: AdaptiveUtilizationPolicyStore,
    var cache: TtlCache<AdaptiveUtilizationPolicy>,
    var hits: Int,
    var misses: Int,
) : AdaptiveUtilizationPolicyStore {
    override fun find(code: String): AdaptiveUtilizationPolicy? {
        val hit = (cache.get(code) as AdaptiveUtilizationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveUtilizationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveUtilizationPolicy> = inner.list()
}

fun newCachedAdaptiveUtilizationPolicyStore(inner: AdaptiveUtilizationPolicyStore, ttl: Int, clock: Clock): AdaptiveUtilizationPolicyStore {
    return CachedAdaptiveUtilizationPolicyStore(
        inner = inner,
        cache = newTtlCache<AdaptiveUtilizationPolicy>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive utilization policies. Empty fields match all. */
data class AdaptiveUtilizationPolicyQuery(
    var country: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveUtilizationPolicy): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveUtilizationPolicy>): MutableList<AdaptiveUtilizationPolicy> {
        val matched = filter(items) { candidate: AdaptiveUtilizationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveUtilizationPolicies(store: AdaptiveUtilizationPolicyStore, query: AdaptiveUtilizationPolicyQuery): MutableList<AdaptiveUtilizationPolicy> {
    return query.apply(store.list())
}
