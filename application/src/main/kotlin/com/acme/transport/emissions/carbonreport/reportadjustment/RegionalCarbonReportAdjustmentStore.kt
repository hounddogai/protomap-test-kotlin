package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional carbon report adjustments by code. */
interface RegionalCarbonReportAdjustmentStore {
    fun find(code: String): RegionalCarbonReportAdjustment?
    fun save(item: RegionalCarbonReportAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCarbonReportAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCarbonReportAdjustmentStore(
    var items: MutableMap<String, RegionalCarbonReportAdjustment>,
    var order: MutableList<String>,
) : RegionalCarbonReportAdjustmentStore {
    override fun find(code: String): RegionalCarbonReportAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCarbonReportAdjustment) {
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

    override fun list(): MutableList<RegionalCarbonReportAdjustment> {
        val out = mutableListOf<RegionalCarbonReportAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCarbonReportAdjustmentStore(): RegionalCarbonReportAdjustmentStore {
    return MemoryRegionalCarbonReportAdjustmentStore(
        mutableMapOf<String, RegionalCarbonReportAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCarbonReportAdjustmentStore(
    var inner: RegionalCarbonReportAdjustmentStore,
    var cache: TtlCache<RegionalCarbonReportAdjustment>,
    var hits: Int,
    var misses: Int,
) : RegionalCarbonReportAdjustmentStore {
    override fun find(code: String): RegionalCarbonReportAdjustment? {
        val hit = (cache.get(code) as RegionalCarbonReportAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCarbonReportAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCarbonReportAdjustment> = inner.list()
}

fun newCachedRegionalCarbonReportAdjustmentStore(inner: RegionalCarbonReportAdjustmentStore, ttl: Int, clock: Clock): RegionalCarbonReportAdjustmentStore {
    return CachedRegionalCarbonReportAdjustmentStore(
        inner = inner,
        cache = newTtlCache<RegionalCarbonReportAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional carbon report adjustments. Empty fields match all. */
data class RegionalCarbonReportAdjustmentQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCarbonReportAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCarbonReportAdjustment>): MutableList<RegionalCarbonReportAdjustment> {
        val matched = filter(items) { candidate: RegionalCarbonReportAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCarbonReportAdjustments(store: RegionalCarbonReportAdjustmentStore, query: RegionalCarbonReportAdjustmentQuery): MutableList<RegionalCarbonReportAdjustment> {
    return query.apply(store.list())
}
