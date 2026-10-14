package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient duty period segments by code. */
interface LenientDutyPeriodSegmentStore {
    fun find(code: String): LenientDutyPeriodSegment?
    fun save(item: LenientDutyPeriodSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDutyPeriodSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDutyPeriodSegmentStore(
    var items: MutableMap<String, LenientDutyPeriodSegment>,
    var order: MutableList<String>,
) : LenientDutyPeriodSegmentStore {
    override fun find(code: String): LenientDutyPeriodSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDutyPeriodSegment) {
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

    override fun list(): MutableList<LenientDutyPeriodSegment> {
        val out = mutableListOf<LenientDutyPeriodSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDutyPeriodSegmentStore(): LenientDutyPeriodSegmentStore {
    return MemoryLenientDutyPeriodSegmentStore(
        mutableMapOf<String, LenientDutyPeriodSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDutyPeriodSegmentStore(
    var inner: LenientDutyPeriodSegmentStore,
    var cache: TtlCache<LenientDutyPeriodSegment>,
    var hits: Int,
    var misses: Int,
) : LenientDutyPeriodSegmentStore {
    override fun find(code: String): LenientDutyPeriodSegment? {
        val hit = (cache.get(code) as LenientDutyPeriodSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDutyPeriodSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDutyPeriodSegment> = inner.list()
}

fun newCachedLenientDutyPeriodSegmentStore(inner: LenientDutyPeriodSegmentStore, ttl: Int, clock: Clock): LenientDutyPeriodSegmentStore {
    return CachedLenientDutyPeriodSegmentStore(
        inner = inner,
        cache = newTtlCache<LenientDutyPeriodSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient duty period segments. Empty fields match all. */
data class LenientDutyPeriodSegmentQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDutyPeriodSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDutyPeriodSegment>): MutableList<LenientDutyPeriodSegment> {
        val matched = filter(items) { candidate: LenientDutyPeriodSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDutyPeriodSegments(store: LenientDutyPeriodSegmentStore, query: LenientDutyPeriodSegmentQuery): MutableList<LenientDutyPeriodSegment> {
    return query.apply(store.list())
}
