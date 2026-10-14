package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive duty period segments by code. */
interface AdaptiveDutyPeriodSegmentStore {
    fun find(code: String): AdaptiveDutyPeriodSegment?
    fun save(item: AdaptiveDutyPeriodSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDutyPeriodSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDutyPeriodSegmentStore(
    var items: MutableMap<String, AdaptiveDutyPeriodSegment>,
    var order: MutableList<String>,
) : AdaptiveDutyPeriodSegmentStore {
    override fun find(code: String): AdaptiveDutyPeriodSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDutyPeriodSegment) {
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

    override fun list(): MutableList<AdaptiveDutyPeriodSegment> {
        val out = mutableListOf<AdaptiveDutyPeriodSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDutyPeriodSegmentStore(): AdaptiveDutyPeriodSegmentStore {
    return MemoryAdaptiveDutyPeriodSegmentStore(
        mutableMapOf<String, AdaptiveDutyPeriodSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDutyPeriodSegmentStore(
    var inner: AdaptiveDutyPeriodSegmentStore,
    var cache: TtlCache<AdaptiveDutyPeriodSegment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDutyPeriodSegmentStore {
    override fun find(code: String): AdaptiveDutyPeriodSegment? {
        val hit = (cache.get(code) as AdaptiveDutyPeriodSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDutyPeriodSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDutyPeriodSegment> = inner.list()
}

fun newCachedAdaptiveDutyPeriodSegmentStore(inner: AdaptiveDutyPeriodSegmentStore, ttl: Int, clock: Clock): AdaptiveDutyPeriodSegmentStore {
    return CachedAdaptiveDutyPeriodSegmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDutyPeriodSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive duty period segments. Empty fields match all. */
data class AdaptiveDutyPeriodSegmentQuery(
    var region: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDutyPeriodSegment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDutyPeriodSegment>): MutableList<AdaptiveDutyPeriodSegment> {
        val matched = filter(items) { candidate: AdaptiveDutyPeriodSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDutyPeriodSegments(store: AdaptiveDutyPeriodSegmentStore, query: AdaptiveDutyPeriodSegmentQuery): MutableList<AdaptiveDutyPeriodSegment> {
    return query.apply(store.list())
}
