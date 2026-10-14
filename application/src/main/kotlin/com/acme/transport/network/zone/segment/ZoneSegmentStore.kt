package com.acme.transport.network.zone.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone segments by code. */
interface ZoneSegmentStore {
    fun find(code: String): ZoneSegment?
    fun save(item: ZoneSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneSegmentStore(
    var items: MutableMap<String, ZoneSegment>,
    var order: MutableList<String>,
) : ZoneSegmentStore {
    override fun find(code: String): ZoneSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneSegment) {
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

    override fun list(): MutableList<ZoneSegment> {
        val out = mutableListOf<ZoneSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneSegmentStore(): ZoneSegmentStore {
    return MemoryZoneSegmentStore(mutableMapOf<String, ZoneSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneSegmentStore(
    var inner: ZoneSegmentStore,
    var cache: TtlCache<ZoneSegment>,
    var hits: Int,
    var misses: Int,
) : ZoneSegmentStore {
    override fun find(code: String): ZoneSegment? {
        val hit = (cache.get(code) as ZoneSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneSegment> = inner.list()
}

fun newCachedZoneSegmentStore(inner: ZoneSegmentStore, ttl: Int, clock: Clock): ZoneSegmentStore {
    return CachedZoneSegmentStore(
        inner = inner,
        cache = newTtlCache<ZoneSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone segments. Empty fields match all. */
data class ZoneSegmentQuery(
    var segment: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneSegment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<ZoneSegment>): MutableList<ZoneSegment> {
        val matched = filter(items) { candidate: ZoneSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneSegments(store: ZoneSegmentStore, query: ZoneSegmentQuery): MutableList<ZoneSegment> {
    return query.apply(store.list())
}
