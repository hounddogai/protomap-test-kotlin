package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report estimates by code. */
interface CarbonReportEstimateStore {
    fun find(code: String): CarbonReportEstimate?
    fun save(item: CarbonReportEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportEstimateStore(
    var items: MutableMap<String, CarbonReportEstimate>,
    var order: MutableList<String>,
) : CarbonReportEstimateStore {
    override fun find(code: String): CarbonReportEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportEstimate) {
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

    override fun list(): MutableList<CarbonReportEstimate> {
        val out = mutableListOf<CarbonReportEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportEstimateStore(): CarbonReportEstimateStore {
    return MemoryCarbonReportEstimateStore(
        mutableMapOf<String, CarbonReportEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportEstimateStore(
    var inner: CarbonReportEstimateStore,
    var cache: TtlCache<CarbonReportEstimate>,
    var hits: Int,
    var misses: Int,
) : CarbonReportEstimateStore {
    override fun find(code: String): CarbonReportEstimate? {
        val hit = (cache.get(code) as CarbonReportEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportEstimate> = inner.list()
}

fun newCachedCarbonReportEstimateStore(inner: CarbonReportEstimateStore, ttl: Int, clock: Clock): CarbonReportEstimateStore {
    return CachedCarbonReportEstimateStore(
        inner = inner,
        cache = newTtlCache<CarbonReportEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report estimates. Empty fields match all. */
data class CarbonReportEstimateQuery(
    var depot: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportEstimate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportEstimate>): MutableList<CarbonReportEstimate> {
        val matched = filter(items) { candidate: CarbonReportEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportEstimates(store: CarbonReportEstimateStore, query: CarbonReportEstimateQuery): MutableList<CarbonReportEstimate> {
    return query.apply(store.list())
}
