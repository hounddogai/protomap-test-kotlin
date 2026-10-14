package com.acme.transport.fleetops.tire.segment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted tire segments by code. */
interface WeightedTireSegmentStore {
    fun find(code: String): WeightedTireSegment?
    fun save(item: WeightedTireSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTireSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTireSegmentStore(
    var items: MutableMap<String, WeightedTireSegment>,
    var order: MutableList<String>,
) : WeightedTireSegmentStore {
    override fun find(code: String): WeightedTireSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTireSegment) {
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

    override fun list(): MutableList<WeightedTireSegment> {
        val out = mutableListOf<WeightedTireSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTireSegmentStore(): WeightedTireSegmentStore {
    return MemoryWeightedTireSegmentStore(mutableMapOf<String, WeightedTireSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTireSegmentStore(
    var inner: WeightedTireSegmentStore,
    var cache: TtlCache<WeightedTireSegment>,
    var hits: Int,
    var misses: Int,
) : WeightedTireSegmentStore {
    override fun find(code: String): WeightedTireSegment? {
        val hit = (cache.get(code) as WeightedTireSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTireSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTireSegment> = inner.list()
}

fun newCachedWeightedTireSegmentStore(inner: WeightedTireSegmentStore, ttl: Int, clock: Clock): WeightedTireSegmentStore {
    return CachedWeightedTireSegmentStore(
        inner = inner,
        cache = newTtlCache<WeightedTireSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted tire segments. Empty fields match all. */
data class WeightedTireSegmentQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTireSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTireSegment>): MutableList<WeightedTireSegment> {
        val matched = filter(items) { candidate: WeightedTireSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTireSegments(store: WeightedTireSegmentStore, query: WeightedTireSegmentQuery): MutableList<WeightedTireSegment> {
    return query.apply(store.list())
}
