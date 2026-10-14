package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional carbon report budgets by code. */
interface RegionalCarbonReportBudgetStore {
    fun find(code: String): RegionalCarbonReportBudget?
    fun save(item: RegionalCarbonReportBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCarbonReportBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCarbonReportBudgetStore(
    var items: MutableMap<String, RegionalCarbonReportBudget>,
    var order: MutableList<String>,
) : RegionalCarbonReportBudgetStore {
    override fun find(code: String): RegionalCarbonReportBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCarbonReportBudget) {
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

    override fun list(): MutableList<RegionalCarbonReportBudget> {
        val out = mutableListOf<RegionalCarbonReportBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCarbonReportBudgetStore(): RegionalCarbonReportBudgetStore {
    return MemoryRegionalCarbonReportBudgetStore(
        mutableMapOf<String, RegionalCarbonReportBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCarbonReportBudgetStore(
    var inner: RegionalCarbonReportBudgetStore,
    var cache: TtlCache<RegionalCarbonReportBudget>,
    var hits: Int,
    var misses: Int,
) : RegionalCarbonReportBudgetStore {
    override fun find(code: String): RegionalCarbonReportBudget? {
        val hit = (cache.get(code) as RegionalCarbonReportBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCarbonReportBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCarbonReportBudget> = inner.list()
}

fun newCachedRegionalCarbonReportBudgetStore(inner: RegionalCarbonReportBudgetStore, ttl: Int, clock: Clock): RegionalCarbonReportBudgetStore {
    return CachedRegionalCarbonReportBudgetStore(
        inner = inner,
        cache = newTtlCache<RegionalCarbonReportBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional carbon report budgets. Empty fields match all. */
data class RegionalCarbonReportBudgetQuery(
    var locale: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCarbonReportBudget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<RegionalCarbonReportBudget>): MutableList<RegionalCarbonReportBudget> {
        val matched = filter(items) { candidate: RegionalCarbonReportBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCarbonReportBudgets(store: RegionalCarbonReportBudgetStore, query: RegionalCarbonReportBudgetQuery): MutableList<RegionalCarbonReportBudget> {
    return query.apply(store.list())
}
