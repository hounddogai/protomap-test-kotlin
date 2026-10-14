package com.acme.transport.network.zone.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional zone segments by code. */
interface RegionalZoneSegmentStore {
    fun find(code: String): RegionalZoneSegment?
    fun save(item: RegionalZoneSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalZoneSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalZoneSegmentStore(
    var items: MutableMap<String, RegionalZoneSegment>,
    var order: MutableList<String>,
) : RegionalZoneSegmentStore {
    override fun find(code: String): RegionalZoneSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalZoneSegment) {
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

    override fun list(): MutableList<RegionalZoneSegment> {
        val out = mutableListOf<RegionalZoneSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalZoneSegmentStore(): RegionalZoneSegmentStore {
    return MemoryRegionalZoneSegmentStore(mutableMapOf<String, RegionalZoneSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalZoneSegmentStore(
    var inner: RegionalZoneSegmentStore,
    var cache: TtlCache<RegionalZoneSegment>,
    var hits: Int,
    var misses: Int,
) : RegionalZoneSegmentStore {
    override fun find(code: String): RegionalZoneSegment? {
        val hit = (cache.get(code) as RegionalZoneSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalZoneSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalZoneSegment> = inner.list()
}

fun newCachedRegionalZoneSegmentStore(inner: RegionalZoneSegmentStore, ttl: Int, clock: Clock): RegionalZoneSegmentStore {
    return CachedRegionalZoneSegmentStore(
        inner = inner,
        cache = newTtlCache<RegionalZoneSegment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional zone segments. Empty fields match all. */
data class RegionalZoneSegmentQuery(
    var unit: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalZoneSegment): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalZoneSegment>): MutableList<RegionalZoneSegment> {
        val matched = filter(items) { candidate: RegionalZoneSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalZoneSegments(store: RegionalZoneSegmentStore, query: RegionalZoneSegmentQuery): MutableList<RegionalZoneSegment> {
    return query.apply(store.list())
}
