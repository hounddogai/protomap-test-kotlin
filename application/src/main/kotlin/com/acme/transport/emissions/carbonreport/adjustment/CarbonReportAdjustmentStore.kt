package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report adjustments by code. */
interface CarbonReportAdjustmentStore {
    fun find(code: String): CarbonReportAdjustment?
    fun save(item: CarbonReportAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportAdjustmentStore(
    var items: MutableMap<String, CarbonReportAdjustment>,
    var order: MutableList<String>,
) : CarbonReportAdjustmentStore {
    override fun find(code: String): CarbonReportAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportAdjustment) {
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

    override fun list(): MutableList<CarbonReportAdjustment> {
        val out = mutableListOf<CarbonReportAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportAdjustmentStore(): CarbonReportAdjustmentStore {
    return MemoryCarbonReportAdjustmentStore(
        mutableMapOf<String, CarbonReportAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportAdjustmentStore(
    var inner: CarbonReportAdjustmentStore,
    var cache: TtlCache<CarbonReportAdjustment>,
    var hits: Int,
    var misses: Int,
) : CarbonReportAdjustmentStore {
    override fun find(code: String): CarbonReportAdjustment? {
        val hit = (cache.get(code) as CarbonReportAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportAdjustment> = inner.list()
}

fun newCachedCarbonReportAdjustmentStore(inner: CarbonReportAdjustmentStore, ttl: Int, clock: Clock): CarbonReportAdjustmentStore {
    return CachedCarbonReportAdjustmentStore(
        inner = inner,
        cache = newTtlCache<CarbonReportAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report adjustments. Empty fields match all. */
data class CarbonReportAdjustmentQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportAdjustment>): MutableList<CarbonReportAdjustment> {
        val matched = filter(items) { candidate: CarbonReportAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportAdjustments(store: CarbonReportAdjustmentStore, query: CarbonReportAdjustmentQuery): MutableList<CarbonReportAdjustment> {
    return query.apply(store.list())
}
