package com.acme.transport.crewing.qualification.template

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped qualification segments by code. */
interface CappedQualificationSegmentStore {
    fun find(code: String): CappedQualificationSegment?
    fun save(item: CappedQualificationSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedQualificationSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedQualificationSegmentStore(
    var items: MutableMap<String, CappedQualificationSegment>,
    var order: MutableList<String>,
) : CappedQualificationSegmentStore {
    override fun find(code: String): CappedQualificationSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedQualificationSegment) {
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

    override fun list(): MutableList<CappedQualificationSegment> {
        val out = mutableListOf<CappedQualificationSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedQualificationSegmentStore(): CappedQualificationSegmentStore {
    return MemoryCappedQualificationSegmentStore(
        mutableMapOf<String, CappedQualificationSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedQualificationSegmentStore(
    var inner: CappedQualificationSegmentStore,
    var cache: TtlCache<CappedQualificationSegment>,
    var hits: Int,
    var misses: Int,
) : CappedQualificationSegmentStore {
    override fun find(code: String): CappedQualificationSegment? {
        val hit = (cache.get(code) as CappedQualificationSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedQualificationSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedQualificationSegment> = inner.list()
}

fun newCachedCappedQualificationSegmentStore(inner: CappedQualificationSegmentStore, ttl: Int, clock: Clock): CappedQualificationSegmentStore {
    return CachedCappedQualificationSegmentStore(
        inner = inner,
        cache = newTtlCache<CappedQualificationSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped qualification segments. Empty fields match all. */
data class CappedQualificationSegmentQuery(
    var lane: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedQualificationSegment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedQualificationSegment>): MutableList<CappedQualificationSegment> {
        val matched = filter(items) { candidate: CappedQualificationSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedQualificationSegments(store: CappedQualificationSegmentStore, query: CappedQualificationSegmentQuery): MutableList<CappedQualificationSegment> {
    return query.apply(store.list())
}
