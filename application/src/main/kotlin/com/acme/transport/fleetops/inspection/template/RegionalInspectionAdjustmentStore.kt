package com.acme.transport.fleetops.inspection.template

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional inspection adjustments by code. */
interface RegionalInspectionAdjustmentStore {
    fun find(code: String): RegionalInspectionAdjustment?
    fun save(item: RegionalInspectionAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalInspectionAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalInspectionAdjustmentStore(
    var items: MutableMap<String, RegionalInspectionAdjustment>,
    var order: MutableList<String>,
) : RegionalInspectionAdjustmentStore {
    override fun find(code: String): RegionalInspectionAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalInspectionAdjustment) {
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

    override fun list(): MutableList<RegionalInspectionAdjustment> {
        val out = mutableListOf<RegionalInspectionAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalInspectionAdjustmentStore(): RegionalInspectionAdjustmentStore {
    return MemoryRegionalInspectionAdjustmentStore(
        mutableMapOf<String, RegionalInspectionAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalInspectionAdjustmentStore(
    var inner: RegionalInspectionAdjustmentStore,
    var cache: TtlCache<RegionalInspectionAdjustment>,
    var hits: Int,
    var misses: Int,
) : RegionalInspectionAdjustmentStore {
    override fun find(code: String): RegionalInspectionAdjustment? {
        val hit = (cache.get(code) as RegionalInspectionAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalInspectionAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalInspectionAdjustment> = inner.list()
}

fun newCachedRegionalInspectionAdjustmentStore(inner: RegionalInspectionAdjustmentStore, ttl: Int, clock: Clock): RegionalInspectionAdjustmentStore {
    return CachedRegionalInspectionAdjustmentStore(
        inner = inner,
        cache = newTtlCache<RegionalInspectionAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional inspection adjustments. Empty fields match all. */
data class RegionalInspectionAdjustmentQuery(
    var lane: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalInspectionAdjustment): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalInspectionAdjustment>): MutableList<RegionalInspectionAdjustment> {
        val matched = filter(items) { candidate: RegionalInspectionAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalInspectionAdjustments(store: RegionalInspectionAdjustmentStore, query: RegionalInspectionAdjustmentQuery): MutableList<RegionalInspectionAdjustment> {
    return query.apply(store.list())
}
