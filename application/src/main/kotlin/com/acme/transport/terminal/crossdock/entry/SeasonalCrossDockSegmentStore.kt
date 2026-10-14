package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal cross dock segments by code. */
interface SeasonalCrossDockSegmentStore {
    fun find(code: String): SeasonalCrossDockSegment?
    fun save(item: SeasonalCrossDockSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCrossDockSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCrossDockSegmentStore(
    var items: MutableMap<String, SeasonalCrossDockSegment>,
    var order: MutableList<String>,
) : SeasonalCrossDockSegmentStore {
    override fun find(code: String): SeasonalCrossDockSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCrossDockSegment) {
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

    override fun list(): MutableList<SeasonalCrossDockSegment> {
        val out = mutableListOf<SeasonalCrossDockSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCrossDockSegmentStore(): SeasonalCrossDockSegmentStore {
    return MemorySeasonalCrossDockSegmentStore(
        mutableMapOf<String, SeasonalCrossDockSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCrossDockSegmentStore(
    var inner: SeasonalCrossDockSegmentStore,
    var cache: TtlCache<SeasonalCrossDockSegment>,
    var hits: Int,
    var misses: Int,
) : SeasonalCrossDockSegmentStore {
    override fun find(code: String): SeasonalCrossDockSegment? {
        val hit = (cache.get(code) as SeasonalCrossDockSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCrossDockSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCrossDockSegment> = inner.list()
}

fun newCachedSeasonalCrossDockSegmentStore(inner: SeasonalCrossDockSegmentStore, ttl: Int, clock: Clock): SeasonalCrossDockSegmentStore {
    return CachedSeasonalCrossDockSegmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalCrossDockSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal cross dock segments. Empty fields match all. */
data class SeasonalCrossDockSegmentQuery(
    var lane: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCrossDockSegment): Boolean {
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

    fun apply(items: MutableList<SeasonalCrossDockSegment>): MutableList<SeasonalCrossDockSegment> {
        val matched = filter(items) { candidate: SeasonalCrossDockSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCrossDockSegments(store: SeasonalCrossDockSegmentStore, query: SeasonalCrossDockSegmentQuery): MutableList<SeasonalCrossDockSegment> {
    return query.apply(store.list())
}
