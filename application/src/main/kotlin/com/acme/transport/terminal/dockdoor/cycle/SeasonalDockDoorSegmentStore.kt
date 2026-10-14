package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal dock door segments by code. */
interface SeasonalDockDoorSegmentStore {
    fun find(code: String): SeasonalDockDoorSegment?
    fun save(item: SeasonalDockDoorSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDockDoorSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDockDoorSegmentStore(
    var items: MutableMap<String, SeasonalDockDoorSegment>,
    var order: MutableList<String>,
) : SeasonalDockDoorSegmentStore {
    override fun find(code: String): SeasonalDockDoorSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDockDoorSegment) {
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

    override fun list(): MutableList<SeasonalDockDoorSegment> {
        val out = mutableListOf<SeasonalDockDoorSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDockDoorSegmentStore(): SeasonalDockDoorSegmentStore {
    return MemorySeasonalDockDoorSegmentStore(
        mutableMapOf<String, SeasonalDockDoorSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDockDoorSegmentStore(
    var inner: SeasonalDockDoorSegmentStore,
    var cache: TtlCache<SeasonalDockDoorSegment>,
    var hits: Int,
    var misses: Int,
) : SeasonalDockDoorSegmentStore {
    override fun find(code: String): SeasonalDockDoorSegment? {
        val hit = (cache.get(code) as SeasonalDockDoorSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDockDoorSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDockDoorSegment> = inner.list()
}

fun newCachedSeasonalDockDoorSegmentStore(inner: SeasonalDockDoorSegmentStore, ttl: Int, clock: Clock): SeasonalDockDoorSegmentStore {
    return CachedSeasonalDockDoorSegmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalDockDoorSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal dock door segments. Empty fields match all. */
data class SeasonalDockDoorSegmentQuery(
    var region: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDockDoorSegment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<SeasonalDockDoorSegment>): MutableList<SeasonalDockDoorSegment> {
        val matched = filter(items) { candidate: SeasonalDockDoorSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDockDoorSegments(store: SeasonalDockDoorSegmentStore, query: SeasonalDockDoorSegmentQuery): MutableList<SeasonalDockDoorSegment> {
    return query.apply(store.list())
}
