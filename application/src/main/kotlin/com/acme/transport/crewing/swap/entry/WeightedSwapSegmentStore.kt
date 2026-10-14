package com.acme.transport.crewing.swap.entry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted swap segments by code. */
interface WeightedSwapSegmentStore {
    fun find(code: String): WeightedSwapSegment?
    fun save(item: WeightedSwapSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedSwapSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedSwapSegmentStore(
    var items: MutableMap<String, WeightedSwapSegment>,
    var order: MutableList<String>,
) : WeightedSwapSegmentStore {
    override fun find(code: String): WeightedSwapSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedSwapSegment) {
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

    override fun list(): MutableList<WeightedSwapSegment> {
        val out = mutableListOf<WeightedSwapSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedSwapSegmentStore(): WeightedSwapSegmentStore {
    return MemoryWeightedSwapSegmentStore(mutableMapOf<String, WeightedSwapSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedSwapSegmentStore(
    var inner: WeightedSwapSegmentStore,
    var cache: TtlCache<WeightedSwapSegment>,
    var hits: Int,
    var misses: Int,
) : WeightedSwapSegmentStore {
    override fun find(code: String): WeightedSwapSegment? {
        val hit = (cache.get(code) as WeightedSwapSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedSwapSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedSwapSegment> = inner.list()
}

fun newCachedWeightedSwapSegmentStore(inner: WeightedSwapSegmentStore, ttl: Int, clock: Clock): WeightedSwapSegmentStore {
    return CachedWeightedSwapSegmentStore(
        inner = inner,
        cache = newTtlCache<WeightedSwapSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted swap segments. Empty fields match all. */
data class WeightedSwapSegmentQuery(
    var unit: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedSwapSegment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedSwapSegment>): MutableList<WeightedSwapSegment> {
        val matched = filter(items) { candidate: WeightedSwapSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedSwapSegments(store: WeightedSwapSegmentStore, query: WeightedSwapSegmentQuery): MutableList<WeightedSwapSegment> {
    return query.apply(store.list())
}
