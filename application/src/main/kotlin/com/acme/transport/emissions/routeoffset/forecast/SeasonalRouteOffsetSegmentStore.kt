package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal route offset segments by code. */
interface SeasonalRouteOffsetSegmentStore {
    fun find(code: String): SeasonalRouteOffsetSegment?
    fun save(item: SeasonalRouteOffsetSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRouteOffsetSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRouteOffsetSegmentStore(
    var items: MutableMap<String, SeasonalRouteOffsetSegment>,
    var order: MutableList<String>,
) : SeasonalRouteOffsetSegmentStore {
    override fun find(code: String): SeasonalRouteOffsetSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRouteOffsetSegment) {
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

    override fun list(): MutableList<SeasonalRouteOffsetSegment> {
        val out = mutableListOf<SeasonalRouteOffsetSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRouteOffsetSegmentStore(): SeasonalRouteOffsetSegmentStore {
    return MemorySeasonalRouteOffsetSegmentStore(
        mutableMapOf<String, SeasonalRouteOffsetSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRouteOffsetSegmentStore(
    var inner: SeasonalRouteOffsetSegmentStore,
    var cache: TtlCache<SeasonalRouteOffsetSegment>,
    var hits: Int,
    var misses: Int,
) : SeasonalRouteOffsetSegmentStore {
    override fun find(code: String): SeasonalRouteOffsetSegment? {
        val hit = (cache.get(code) as SeasonalRouteOffsetSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRouteOffsetSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRouteOffsetSegment> = inner.list()
}

fun newCachedSeasonalRouteOffsetSegmentStore(inner: SeasonalRouteOffsetSegmentStore, ttl: Int, clock: Clock): SeasonalRouteOffsetSegmentStore {
    return CachedSeasonalRouteOffsetSegmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalRouteOffsetSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal route offset segments. Empty fields match all. */
data class SeasonalRouteOffsetSegmentQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRouteOffsetSegment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRouteOffsetSegment>): MutableList<SeasonalRouteOffsetSegment> {
        val matched = filter(items) { candidate: SeasonalRouteOffsetSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRouteOffsetSegments(store: SeasonalRouteOffsetSegmentStore, query: SeasonalRouteOffsetSegmentQuery): MutableList<SeasonalRouteOffsetSegment> {
    return query.apply(store.list())
}
