package com.acme.transport.crewing.swap.mapping

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal swap segments by code. */
interface SeasonalSwapSegmentStore {
    fun find(code: String): SeasonalSwapSegment?
    fun save(item: SeasonalSwapSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalSwapSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalSwapSegmentStore(
    var items: MutableMap<String, SeasonalSwapSegment>,
    var order: MutableList<String>,
) : SeasonalSwapSegmentStore {
    override fun find(code: String): SeasonalSwapSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalSwapSegment) {
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

    override fun list(): MutableList<SeasonalSwapSegment> {
        val out = mutableListOf<SeasonalSwapSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalSwapSegmentStore(): SeasonalSwapSegmentStore {
    return MemorySeasonalSwapSegmentStore(mutableMapOf<String, SeasonalSwapSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalSwapSegmentStore(
    var inner: SeasonalSwapSegmentStore,
    var cache: TtlCache<SeasonalSwapSegment>,
    var hits: Int,
    var misses: Int,
) : SeasonalSwapSegmentStore {
    override fun find(code: String): SeasonalSwapSegment? {
        val hit = (cache.get(code) as SeasonalSwapSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalSwapSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalSwapSegment> = inner.list()
}

fun newCachedSeasonalSwapSegmentStore(inner: SeasonalSwapSegmentStore, ttl: Int, clock: Clock): SeasonalSwapSegmentStore {
    return CachedSeasonalSwapSegmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalSwapSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal swap segments. Empty fields match all. */
data class SeasonalSwapSegmentQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalSwapSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<SeasonalSwapSegment>): MutableList<SeasonalSwapSegment> {
        val matched = filter(items) { candidate: SeasonalSwapSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalSwapSegments(store: SeasonalSwapSegmentStore, query: SeasonalSwapSegmentQuery): MutableList<SeasonalSwapSegment> {
    return query.apply(store.list())
}
