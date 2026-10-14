package com.acme.transport.terminal.dwell.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted dwell segments by code. */
interface WeightedDwellSegmentStore {
    fun find(code: String): WeightedDwellSegment?
    fun save(item: WeightedDwellSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedDwellSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedDwellSegmentStore(
    var items: MutableMap<String, WeightedDwellSegment>,
    var order: MutableList<String>,
) : WeightedDwellSegmentStore {
    override fun find(code: String): WeightedDwellSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedDwellSegment) {
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

    override fun list(): MutableList<WeightedDwellSegment> {
        val out = mutableListOf<WeightedDwellSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedDwellSegmentStore(): WeightedDwellSegmentStore {
    return MemoryWeightedDwellSegmentStore(
        mutableMapOf<String, WeightedDwellSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedDwellSegmentStore(
    var inner: WeightedDwellSegmentStore,
    var cache: TtlCache<WeightedDwellSegment>,
    var hits: Int,
    var misses: Int,
) : WeightedDwellSegmentStore {
    override fun find(code: String): WeightedDwellSegment? {
        val hit = (cache.get(code) as WeightedDwellSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedDwellSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedDwellSegment> = inner.list()
}

fun newCachedWeightedDwellSegmentStore(inner: WeightedDwellSegmentStore, ttl: Int, clock: Clock): WeightedDwellSegmentStore {
    return CachedWeightedDwellSegmentStore(
        inner = inner,
        cache = newTtlCache<WeightedDwellSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted dwell segments. Empty fields match all. */
data class WeightedDwellSegmentQuery(
    var region: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedDwellSegment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedDwellSegment>): MutableList<WeightedDwellSegment> {
        val matched = filter(items) { candidate: WeightedDwellSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedDwellSegments(store: WeightedDwellSegmentStore, query: WeightedDwellSegmentQuery): MutableList<WeightedDwellSegment> {
    return query.apply(store.list())
}
