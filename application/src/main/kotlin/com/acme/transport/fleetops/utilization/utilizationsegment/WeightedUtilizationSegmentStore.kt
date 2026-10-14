package com.acme.transport.fleetops.utilization.utilizationsegment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted utilization segments by code. */
interface WeightedUtilizationSegmentStore {
    fun find(code: String): WeightedUtilizationSegment?
    fun save(item: WeightedUtilizationSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedUtilizationSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedUtilizationSegmentStore(
    var items: MutableMap<String, WeightedUtilizationSegment>,
    var order: MutableList<String>,
) : WeightedUtilizationSegmentStore {
    override fun find(code: String): WeightedUtilizationSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedUtilizationSegment) {
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

    override fun list(): MutableList<WeightedUtilizationSegment> {
        val out = mutableListOf<WeightedUtilizationSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedUtilizationSegmentStore(): WeightedUtilizationSegmentStore {
    return MemoryWeightedUtilizationSegmentStore(
        mutableMapOf<String, WeightedUtilizationSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedUtilizationSegmentStore(
    var inner: WeightedUtilizationSegmentStore,
    var cache: TtlCache<WeightedUtilizationSegment>,
    var hits: Int,
    var misses: Int,
) : WeightedUtilizationSegmentStore {
    override fun find(code: String): WeightedUtilizationSegment? {
        val hit = (cache.get(code) as WeightedUtilizationSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedUtilizationSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedUtilizationSegment> = inner.list()
}

fun newCachedWeightedUtilizationSegmentStore(inner: WeightedUtilizationSegmentStore, ttl: Int, clock: Clock): WeightedUtilizationSegmentStore {
    return CachedWeightedUtilizationSegmentStore(
        inner = inner,
        cache = newTtlCache<WeightedUtilizationSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted utilization segments. Empty fields match all. */
data class WeightedUtilizationSegmentQuery(
    var segment: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedUtilizationSegment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedUtilizationSegment>): MutableList<WeightedUtilizationSegment> {
        val matched = filter(items) { candidate: WeightedUtilizationSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedUtilizationSegments(store: WeightedUtilizationSegmentStore, query: WeightedUtilizationSegmentQuery): MutableList<WeightedUtilizationSegment> {
    return query.apply(store.list())
}
