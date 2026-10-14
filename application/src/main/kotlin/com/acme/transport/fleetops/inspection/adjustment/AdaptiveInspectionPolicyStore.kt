package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive inspection policies by code. */
interface AdaptiveInspectionPolicyStore {
    fun find(code: String): AdaptiveInspectionPolicy?
    fun save(item: AdaptiveInspectionPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveInspectionPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveInspectionPolicyStore(
    var items: MutableMap<String, AdaptiveInspectionPolicy>,
    var order: MutableList<String>,
) : AdaptiveInspectionPolicyStore {
    override fun find(code: String): AdaptiveInspectionPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveInspectionPolicy) {
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

    override fun list(): MutableList<AdaptiveInspectionPolicy> {
        val out = mutableListOf<AdaptiveInspectionPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveInspectionPolicyStore(): AdaptiveInspectionPolicyStore {
    return MemoryAdaptiveInspectionPolicyStore(
        mutableMapOf<String, AdaptiveInspectionPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveInspectionPolicyStore(
    var inner: AdaptiveInspectionPolicyStore,
    var cache: TtlCache<AdaptiveInspectionPolicy>,
    var hits: Int,
    var misses: Int,
) : AdaptiveInspectionPolicyStore {
    override fun find(code: String): AdaptiveInspectionPolicy? {
        val hit = (cache.get(code) as AdaptiveInspectionPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveInspectionPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveInspectionPolicy> = inner.list()
}

fun newCachedAdaptiveInspectionPolicyStore(inner: AdaptiveInspectionPolicyStore, ttl: Int, clock: Clock): AdaptiveInspectionPolicyStore {
    return CachedAdaptiveInspectionPolicyStore(
        inner = inner,
        cache = newTtlCache<AdaptiveInspectionPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive inspection policies. Empty fields match all. */
data class AdaptiveInspectionPolicyQuery(
    var lane: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveInspectionPolicy): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveInspectionPolicy>): MutableList<AdaptiveInspectionPolicy> {
        val matched = filter(items) { candidate: AdaptiveInspectionPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveInspectionPolicies(store: AdaptiveInspectionPolicyStore, query: AdaptiveInspectionPolicyQuery): MutableList<AdaptiveInspectionPolicy> {
    return query.apply(store.list())
}
