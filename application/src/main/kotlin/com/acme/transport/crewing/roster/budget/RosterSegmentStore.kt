package com.acme.transport.crewing.roster.budget

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster segments by code. */
interface RosterSegmentStore {
    fun find(code: String): RosterSegment?
    fun save(item: RosterSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterSegmentStore(
    var items: MutableMap<String, RosterSegment>,
    var order: MutableList<String>,
) : RosterSegmentStore {
    override fun find(code: String): RosterSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterSegment) {
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

    override fun list(): MutableList<RosterSegment> {
        val out = mutableListOf<RosterSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterSegmentStore(): RosterSegmentStore {
    return MemoryRosterSegmentStore(mutableMapOf<String, RosterSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterSegmentStore(
    var inner: RosterSegmentStore,
    var cache: TtlCache<RosterSegment>,
    var hits: Int,
    var misses: Int,
) : RosterSegmentStore {
    override fun find(code: String): RosterSegment? {
        val hit = (cache.get(code) as RosterSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterSegment> = inner.list()
}

fun newCachedRosterSegmentStore(inner: RosterSegmentStore, ttl: Int, clock: Clock): RosterSegmentStore {
    return CachedRosterSegmentStore(
        inner = inner,
        cache = newTtlCache<RosterSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster segments. Empty fields match all. */
data class RosterSegmentQuery(
    var segment: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterSegment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterSegment>): MutableList<RosterSegment> {
        val matched = filter(items) { candidate: RosterSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterSegments(store: RosterSegmentStore, query: RosterSegmentQuery): MutableList<RosterSegment> {
    return query.apply(store.list())
}
