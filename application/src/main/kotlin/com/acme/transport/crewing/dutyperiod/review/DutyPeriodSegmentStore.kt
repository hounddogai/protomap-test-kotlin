package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period segments by code. */
interface DutyPeriodSegmentStore {
    fun find(code: String): DutyPeriodSegment?
    fun save(item: DutyPeriodSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodSegmentStore(
    var items: MutableMap<String, DutyPeriodSegment>,
    var order: MutableList<String>,
) : DutyPeriodSegmentStore {
    override fun find(code: String): DutyPeriodSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodSegment) {
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

    override fun list(): MutableList<DutyPeriodSegment> {
        val out = mutableListOf<DutyPeriodSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodSegmentStore(): DutyPeriodSegmentStore {
    return MemoryDutyPeriodSegmentStore(mutableMapOf<String, DutyPeriodSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodSegmentStore(
    var inner: DutyPeriodSegmentStore,
    var cache: TtlCache<DutyPeriodSegment>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodSegmentStore {
    override fun find(code: String): DutyPeriodSegment? {
        val hit = (cache.get(code) as DutyPeriodSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodSegment> = inner.list()
}

fun newCachedDutyPeriodSegmentStore(inner: DutyPeriodSegmentStore, ttl: Int, clock: Clock): DutyPeriodSegmentStore {
    return CachedDutyPeriodSegmentStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period segments. Empty fields match all. */
data class DutyPeriodSegmentQuery(
    var lane: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodSegment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<DutyPeriodSegment>): MutableList<DutyPeriodSegment> {
        val matched = filter(items) { candidate: DutyPeriodSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodSegments(store: DutyPeriodSegmentStore, query: DutyPeriodSegmentQuery): MutableList<DutyPeriodSegment> {
    return query.apply(store.list())
}
