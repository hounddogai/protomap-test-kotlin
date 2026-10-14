package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted carbon report estimates by code. */
interface WeightedCarbonReportEstimateStore {
    fun find(code: String): WeightedCarbonReportEstimate?
    fun save(item: WeightedCarbonReportEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCarbonReportEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCarbonReportEstimateStore(
    var items: MutableMap<String, WeightedCarbonReportEstimate>,
    var order: MutableList<String>,
) : WeightedCarbonReportEstimateStore {
    override fun find(code: String): WeightedCarbonReportEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCarbonReportEstimate) {
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

    override fun list(): MutableList<WeightedCarbonReportEstimate> {
        val out = mutableListOf<WeightedCarbonReportEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCarbonReportEstimateStore(): WeightedCarbonReportEstimateStore {
    return MemoryWeightedCarbonReportEstimateStore(
        mutableMapOf<String, WeightedCarbonReportEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCarbonReportEstimateStore(
    var inner: WeightedCarbonReportEstimateStore,
    var cache: TtlCache<WeightedCarbonReportEstimate>,
    var hits: Int,
    var misses: Int,
) : WeightedCarbonReportEstimateStore {
    override fun find(code: String): WeightedCarbonReportEstimate? {
        val hit = (cache.get(code) as WeightedCarbonReportEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCarbonReportEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCarbonReportEstimate> = inner.list()
}

fun newCachedWeightedCarbonReportEstimateStore(inner: WeightedCarbonReportEstimateStore, ttl: Int, clock: Clock): WeightedCarbonReportEstimateStore {
    return CachedWeightedCarbonReportEstimateStore(
        inner = inner,
        cache = newTtlCache<WeightedCarbonReportEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted carbon report estimates. Empty fields match all. */
data class WeightedCarbonReportEstimateQuery(
    var depot: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCarbonReportEstimate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedCarbonReportEstimate>): MutableList<WeightedCarbonReportEstimate> {
        val matched = filter(items) { candidate: WeightedCarbonReportEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCarbonReportEstimates(store: WeightedCarbonReportEstimateStore, query: WeightedCarbonReportEstimateQuery): MutableList<WeightedCarbonReportEstimate> {
    return query.apply(store.list())
}
