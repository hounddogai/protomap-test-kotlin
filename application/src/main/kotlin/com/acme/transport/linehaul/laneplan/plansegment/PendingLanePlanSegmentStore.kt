package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending lane plan segments by code. */
interface PendingLanePlanSegmentStore {
    fun find(code: String): PendingLanePlanSegment?
    fun save(item: PendingLanePlanSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingLanePlanSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingLanePlanSegmentStore(
    var items: MutableMap<String, PendingLanePlanSegment>,
    var order: MutableList<String>,
) : PendingLanePlanSegmentStore {
    override fun find(code: String): PendingLanePlanSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingLanePlanSegment) {
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

    override fun list(): MutableList<PendingLanePlanSegment> {
        val out = mutableListOf<PendingLanePlanSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingLanePlanSegmentStore(): PendingLanePlanSegmentStore {
    return MemoryPendingLanePlanSegmentStore(
        mutableMapOf<String, PendingLanePlanSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingLanePlanSegmentStore(
    var inner: PendingLanePlanSegmentStore,
    var cache: TtlCache<PendingLanePlanSegment>,
    var hits: Int,
    var misses: Int,
) : PendingLanePlanSegmentStore {
    override fun find(code: String): PendingLanePlanSegment? {
        val hit = (cache.get(code) as PendingLanePlanSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingLanePlanSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingLanePlanSegment> = inner.list()
}

fun newCachedPendingLanePlanSegmentStore(inner: PendingLanePlanSegmentStore, ttl: Int, clock: Clock): PendingLanePlanSegmentStore {
    return CachedPendingLanePlanSegmentStore(
        inner = inner,
        cache = newTtlCache<PendingLanePlanSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending lane plan segments. Empty fields match all. */
data class PendingLanePlanSegmentQuery(
    var segment: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingLanePlanSegment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingLanePlanSegment>): MutableList<PendingLanePlanSegment> {
        val matched = filter(items) { candidate: PendingLanePlanSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingLanePlanSegments(store: PendingLanePlanSegmentStore, query: PendingLanePlanSegmentQuery): MutableList<PendingLanePlanSegment> {
    return query.apply(store.list())
}
