package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback carbon report budgets by code. */
interface FallbackCarbonReportBudgetStore {
    fun find(code: String): FallbackCarbonReportBudget?
    fun save(item: FallbackCarbonReportBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCarbonReportBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCarbonReportBudgetStore(
    var items: MutableMap<String, FallbackCarbonReportBudget>,
    var order: MutableList<String>,
) : FallbackCarbonReportBudgetStore {
    override fun find(code: String): FallbackCarbonReportBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCarbonReportBudget) {
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

    override fun list(): MutableList<FallbackCarbonReportBudget> {
        val out = mutableListOf<FallbackCarbonReportBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCarbonReportBudgetStore(): FallbackCarbonReportBudgetStore {
    return MemoryFallbackCarbonReportBudgetStore(
        mutableMapOf<String, FallbackCarbonReportBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCarbonReportBudgetStore(
    var inner: FallbackCarbonReportBudgetStore,
    var cache: TtlCache<FallbackCarbonReportBudget>,
    var hits: Int,
    var misses: Int,
) : FallbackCarbonReportBudgetStore {
    override fun find(code: String): FallbackCarbonReportBudget? {
        val hit = (cache.get(code) as FallbackCarbonReportBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCarbonReportBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCarbonReportBudget> = inner.list()
}

fun newCachedFallbackCarbonReportBudgetStore(inner: FallbackCarbonReportBudgetStore, ttl: Int, clock: Clock): FallbackCarbonReportBudgetStore {
    return CachedFallbackCarbonReportBudgetStore(
        inner = inner,
        cache = newTtlCache<FallbackCarbonReportBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback carbon report budgets. Empty fields match all. */
data class FallbackCarbonReportBudgetQuery(
    var depot: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCarbonReportBudget): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackCarbonReportBudget>): MutableList<FallbackCarbonReportBudget> {
        val matched = filter(items) { candidate: FallbackCarbonReportBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCarbonReportBudgets(store: FallbackCarbonReportBudgetStore, query: FallbackCarbonReportBudgetQuery): MutableList<FallbackCarbonReportBudget> {
    return query.apply(store.list())
}
