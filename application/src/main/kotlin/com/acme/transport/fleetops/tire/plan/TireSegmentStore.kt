package com.acme.transport.fleetops.tire.plan

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire segments by code. */
interface TireSegmentStore {
    fun find(code: String): TireSegment?
    fun save(item: TireSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireSegmentStore(
    var items: MutableMap<String, TireSegment>,
    var order: MutableList<String>,
) : TireSegmentStore {
    override fun find(code: String): TireSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireSegment) {
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

    override fun list(): MutableList<TireSegment> {
        val out = mutableListOf<TireSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireSegmentStore(): TireSegmentStore {
    return MemoryTireSegmentStore(mutableMapOf<String, TireSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireSegmentStore(
    var inner: TireSegmentStore,
    var cache: TtlCache<TireSegment>,
    var hits: Int,
    var misses: Int,
) : TireSegmentStore {
    override fun find(code: String): TireSegment? {
        val hit = (cache.get(code) as TireSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireSegment> = inner.list()
}

fun newCachedTireSegmentStore(inner: TireSegmentStore, ttl: Int, clock: Clock): TireSegmentStore {
    return CachedTireSegmentStore(
        inner = inner,
        cache = newTtlCache<TireSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire segments. Empty fields match all. */
data class TireSegmentQuery(
    var depot: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireSegment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<TireSegment>): MutableList<TireSegment> {
        val matched = filter(items) { candidate: TireSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireSegments(store: TireSegmentStore, query: TireSegmentQuery): MutableList<TireSegment> {
    return query.apply(store.list())
}
