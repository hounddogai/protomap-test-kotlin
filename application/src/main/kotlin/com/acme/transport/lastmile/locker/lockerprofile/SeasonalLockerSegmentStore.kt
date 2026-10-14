package com.acme.transport.lastmile.locker.lockerprofile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal locker segments by code. */
interface SeasonalLockerSegmentStore {
    fun find(code: String): SeasonalLockerSegment?
    fun save(item: SeasonalLockerSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalLockerSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalLockerSegmentStore(
    var items: MutableMap<String, SeasonalLockerSegment>,
    var order: MutableList<String>,
) : SeasonalLockerSegmentStore {
    override fun find(code: String): SeasonalLockerSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalLockerSegment) {
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

    override fun list(): MutableList<SeasonalLockerSegment> {
        val out = mutableListOf<SeasonalLockerSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalLockerSegmentStore(): SeasonalLockerSegmentStore {
    return MemorySeasonalLockerSegmentStore(
        mutableMapOf<String, SeasonalLockerSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalLockerSegmentStore(
    var inner: SeasonalLockerSegmentStore,
    var cache: TtlCache<SeasonalLockerSegment>,
    var hits: Int,
    var misses: Int,
) : SeasonalLockerSegmentStore {
    override fun find(code: String): SeasonalLockerSegment? {
        val hit = (cache.get(code) as SeasonalLockerSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalLockerSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalLockerSegment> = inner.list()
}

fun newCachedSeasonalLockerSegmentStore(inner: SeasonalLockerSegmentStore, ttl: Int, clock: Clock): SeasonalLockerSegmentStore {
    return CachedSeasonalLockerSegmentStore(
        inner = inner,
        cache = newTtlCache<SeasonalLockerSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal locker segments. Empty fields match all. */
data class SeasonalLockerSegmentQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalLockerSegment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<SeasonalLockerSegment>): MutableList<SeasonalLockerSegment> {
        val matched = filter(items) { candidate: SeasonalLockerSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalLockerSegments(store: SeasonalLockerSegmentStore, query: SeasonalLockerSegmentQuery): MutableList<SeasonalLockerSegment> {
    return query.apply(store.list())
}
