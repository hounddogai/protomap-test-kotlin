package com.acme.transport.emissions.routeoffset.offsetnotice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict route offset segments by code. */
interface StrictRouteOffsetSegmentStore {
    fun find(code: String): StrictRouteOffsetSegment?
    fun save(item: StrictRouteOffsetSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRouteOffsetSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRouteOffsetSegmentStore(
    var items: MutableMap<String, StrictRouteOffsetSegment>,
    var order: MutableList<String>,
) : StrictRouteOffsetSegmentStore {
    override fun find(code: String): StrictRouteOffsetSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRouteOffsetSegment) {
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

    override fun list(): MutableList<StrictRouteOffsetSegment> {
        val out = mutableListOf<StrictRouteOffsetSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRouteOffsetSegmentStore(): StrictRouteOffsetSegmentStore {
    return MemoryStrictRouteOffsetSegmentStore(
        mutableMapOf<String, StrictRouteOffsetSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRouteOffsetSegmentStore(
    var inner: StrictRouteOffsetSegmentStore,
    var cache: TtlCache<StrictRouteOffsetSegment>,
    var hits: Int,
    var misses: Int,
) : StrictRouteOffsetSegmentStore {
    override fun find(code: String): StrictRouteOffsetSegment? {
        val hit = (cache.get(code) as StrictRouteOffsetSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRouteOffsetSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRouteOffsetSegment> = inner.list()
}

fun newCachedStrictRouteOffsetSegmentStore(inner: StrictRouteOffsetSegmentStore, ttl: Int, clock: Clock): StrictRouteOffsetSegmentStore {
    return CachedStrictRouteOffsetSegmentStore(
        inner = inner,
        cache = newTtlCache<StrictRouteOffsetSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict route offset segments. Empty fields match all. */
data class StrictRouteOffsetSegmentQuery(
    var lane: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRouteOffsetSegment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRouteOffsetSegment>): MutableList<StrictRouteOffsetSegment> {
        val matched = filter(items) { candidate: StrictRouteOffsetSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRouteOffsetSegments(store: StrictRouteOffsetSegmentStore, query: StrictRouteOffsetSegmentQuery): MutableList<StrictRouteOffsetSegment> {
    return query.apply(store.list())
}
