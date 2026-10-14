package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict carbon report estimates by code. */
interface StrictCarbonReportEstimateStore {
    fun find(code: String): StrictCarbonReportEstimate?
    fun save(item: StrictCarbonReportEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictCarbonReportEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictCarbonReportEstimateStore(
    var items: MutableMap<String, StrictCarbonReportEstimate>,
    var order: MutableList<String>,
) : StrictCarbonReportEstimateStore {
    override fun find(code: String): StrictCarbonReportEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictCarbonReportEstimate) {
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

    override fun list(): MutableList<StrictCarbonReportEstimate> {
        val out = mutableListOf<StrictCarbonReportEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictCarbonReportEstimateStore(): StrictCarbonReportEstimateStore {
    return MemoryStrictCarbonReportEstimateStore(
        mutableMapOf<String, StrictCarbonReportEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictCarbonReportEstimateStore(
    var inner: StrictCarbonReportEstimateStore,
    var cache: TtlCache<StrictCarbonReportEstimate>,
    var hits: Int,
    var misses: Int,
) : StrictCarbonReportEstimateStore {
    override fun find(code: String): StrictCarbonReportEstimate? {
        val hit = (cache.get(code) as StrictCarbonReportEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictCarbonReportEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictCarbonReportEstimate> = inner.list()
}

fun newCachedStrictCarbonReportEstimateStore(inner: StrictCarbonReportEstimateStore, ttl: Int, clock: Clock): StrictCarbonReportEstimateStore {
    return CachedStrictCarbonReportEstimateStore(
        inner = inner,
        cache = newTtlCache<StrictCarbonReportEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict carbon report estimates. Empty fields match all. */
data class StrictCarbonReportEstimateQuery(
    var source: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictCarbonReportEstimate): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<StrictCarbonReportEstimate>): MutableList<StrictCarbonReportEstimate> {
        val matched = filter(items) { candidate: StrictCarbonReportEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictCarbonReportEstimates(store: StrictCarbonReportEstimateStore, query: StrictCarbonReportEstimateQuery): MutableList<StrictCarbonReportEstimate> {
    return query.apply(store.list())
}
