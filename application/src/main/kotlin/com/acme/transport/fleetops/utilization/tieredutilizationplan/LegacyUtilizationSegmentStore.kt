package com.acme.transport.fleetops.utilization.tieredutilizationplan

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy utilization segments by code. */
interface LegacyUtilizationSegmentStore {
    fun find(code: String): LegacyUtilizationSegment?
    fun save(item: LegacyUtilizationSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyUtilizationSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyUtilizationSegmentStore(
    var items: MutableMap<String, LegacyUtilizationSegment>,
    var order: MutableList<String>,
) : LegacyUtilizationSegmentStore {
    override fun find(code: String): LegacyUtilizationSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyUtilizationSegment) {
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

    override fun list(): MutableList<LegacyUtilizationSegment> {
        val out = mutableListOf<LegacyUtilizationSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyUtilizationSegmentStore(): LegacyUtilizationSegmentStore {
    return MemoryLegacyUtilizationSegmentStore(
        mutableMapOf<String, LegacyUtilizationSegment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyUtilizationSegmentStore(
    var inner: LegacyUtilizationSegmentStore,
    var cache: TtlCache<LegacyUtilizationSegment>,
    var hits: Int,
    var misses: Int,
) : LegacyUtilizationSegmentStore {
    override fun find(code: String): LegacyUtilizationSegment? {
        val hit = (cache.get(code) as LegacyUtilizationSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyUtilizationSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyUtilizationSegment> = inner.list()
}

fun newCachedLegacyUtilizationSegmentStore(inner: LegacyUtilizationSegmentStore, ttl: Int, clock: Clock): LegacyUtilizationSegmentStore {
    return CachedLegacyUtilizationSegmentStore(
        inner = inner,
        cache = newTtlCache<LegacyUtilizationSegment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy utilization segments. Empty fields match all. */
data class LegacyUtilizationSegmentQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyUtilizationSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LegacyUtilizationSegment>): MutableList<LegacyUtilizationSegment> {
        val matched = filter(items) { candidate: LegacyUtilizationSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyUtilizationSegments(store: LegacyUtilizationSegmentStore, query: LegacyUtilizationSegmentQuery): MutableList<LegacyUtilizationSegment> {
    return query.apply(store.list())
}
