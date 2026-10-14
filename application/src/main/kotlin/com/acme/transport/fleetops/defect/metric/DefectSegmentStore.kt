package com.acme.transport.fleetops.defect.metric

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect segments by code. */
interface DefectSegmentStore {
    fun find(code: String): DefectSegment?
    fun save(item: DefectSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectSegmentStore(
    var items: MutableMap<String, DefectSegment>,
    var order: MutableList<String>,
) : DefectSegmentStore {
    override fun find(code: String): DefectSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectSegment) {
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

    override fun list(): MutableList<DefectSegment> {
        val out = mutableListOf<DefectSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectSegmentStore(): DefectSegmentStore {
    return MemoryDefectSegmentStore(mutableMapOf<String, DefectSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectSegmentStore(
    var inner: DefectSegmentStore,
    var cache: TtlCache<DefectSegment>,
    var hits: Int,
    var misses: Int,
) : DefectSegmentStore {
    override fun find(code: String): DefectSegment? {
        val hit = (cache.get(code) as DefectSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectSegment> = inner.list()
}

fun newCachedDefectSegmentStore(inner: DefectSegmentStore, ttl: Int, clock: Clock): DefectSegmentStore {
    return CachedDefectSegmentStore(
        inner = inner,
        cache = newTtlCache<DefectSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect segments. Empty fields match all. */
data class DefectSegmentQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DefectSegment>): MutableList<DefectSegment> {
        val matched = filter(items) { candidate: DefectSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectSegments(store: DefectSegmentStore, query: DefectSegmentQuery): MutableList<DefectSegment> {
    return query.apply(store.list())
}
