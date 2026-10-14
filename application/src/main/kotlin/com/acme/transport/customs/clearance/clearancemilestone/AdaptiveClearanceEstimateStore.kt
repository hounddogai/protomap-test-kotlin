package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive clearance estimates by code. */
interface AdaptiveClearanceEstimateStore {
    fun find(code: String): AdaptiveClearanceEstimate?
    fun save(item: AdaptiveClearanceEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveClearanceEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveClearanceEstimateStore(
    var items: MutableMap<String, AdaptiveClearanceEstimate>,
    var order: MutableList<String>,
) : AdaptiveClearanceEstimateStore {
    override fun find(code: String): AdaptiveClearanceEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveClearanceEstimate) {
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

    override fun list(): MutableList<AdaptiveClearanceEstimate> {
        val out = mutableListOf<AdaptiveClearanceEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveClearanceEstimateStore(): AdaptiveClearanceEstimateStore {
    return MemoryAdaptiveClearanceEstimateStore(
        mutableMapOf<String, AdaptiveClearanceEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveClearanceEstimateStore(
    var inner: AdaptiveClearanceEstimateStore,
    var cache: TtlCache<AdaptiveClearanceEstimate>,
    var hits: Int,
    var misses: Int,
) : AdaptiveClearanceEstimateStore {
    override fun find(code: String): AdaptiveClearanceEstimate? {
        val hit = (cache.get(code) as AdaptiveClearanceEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveClearanceEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveClearanceEstimate> = inner.list()
}

fun newCachedAdaptiveClearanceEstimateStore(inner: AdaptiveClearanceEstimateStore, ttl: Int, clock: Clock): AdaptiveClearanceEstimateStore {
    return CachedAdaptiveClearanceEstimateStore(
        inner = inner,
        cache = newTtlCache<AdaptiveClearanceEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive clearance estimates. Empty fields match all. */
data class AdaptiveClearanceEstimateQuery(
    var lane: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveClearanceEstimate): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveClearanceEstimate>): MutableList<AdaptiveClearanceEstimate> {
        val matched = filter(items) { candidate: AdaptiveClearanceEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveClearanceEstimates(store: AdaptiveClearanceEstimateStore, query: AdaptiveClearanceEstimateQuery): MutableList<AdaptiveClearanceEstimate> {
    return query.apply(store.list())
}
