package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional utilization segments by code. */
interface RegionalUtilizationSegmentStore {
    fun find(code: String): RegionalUtilizationSegment?
    fun save(item: RegionalUtilizationSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalUtilizationSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalUtilizationSegmentStore(
    var items: MutableMap<String, RegionalUtilizationSegment>,
    var order: MutableList<String>,
) : RegionalUtilizationSegmentStore {
    override fun find(code: String): RegionalUtilizationSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalUtilizationSegment) {
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

    override fun list(): MutableList<RegionalUtilizationSegment> {
        val out = mutableListOf<RegionalUtilizationSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalUtilizationSegmentStore(): RegionalUtilizationSegmentStore {
    return MemoryRegionalUtilizationSegmentStore(
        mutableMapOf<String, RegionalUtilizationSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalUtilizationSegmentStore(
    var inner: RegionalUtilizationSegmentStore,
    var cache: TtlCache<RegionalUtilizationSegment>,
    var hits: Int,
    var misses: Int,
) : RegionalUtilizationSegmentStore {
    override fun find(code: String): RegionalUtilizationSegment? {
        val hit = (cache.get(code) as RegionalUtilizationSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalUtilizationSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalUtilizationSegment> = inner.list()
}

fun newCachedRegionalUtilizationSegmentStore(inner: RegionalUtilizationSegmentStore, ttl: Int, clock: Clock): RegionalUtilizationSegmentStore {
    return CachedRegionalUtilizationSegmentStore(
        inner = inner,
        cache = newTtlCache<RegionalUtilizationSegment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional utilization segments. Empty fields match all. */
data class RegionalUtilizationSegmentQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalUtilizationSegment): Boolean {
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

    fun apply(items: MutableList<RegionalUtilizationSegment>): MutableList<RegionalUtilizationSegment> {
        val matched = filter(items) { candidate: RegionalUtilizationSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalUtilizationSegments(store: RegionalUtilizationSegmentStore, query: RegionalUtilizationSegmentQuery): MutableList<RegionalUtilizationSegment> {
    return query.apply(store.list())
}
