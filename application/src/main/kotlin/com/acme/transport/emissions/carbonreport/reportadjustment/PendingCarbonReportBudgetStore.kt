package com.acme.transport.emissions.carbonreport.reportadjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending carbon report budgets by code. */
interface PendingCarbonReportBudgetStore {
    fun find(code: String): PendingCarbonReportBudget?
    fun save(item: PendingCarbonReportBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCarbonReportBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCarbonReportBudgetStore(
    var items: MutableMap<String, PendingCarbonReportBudget>,
    var order: MutableList<String>,
) : PendingCarbonReportBudgetStore {
    override fun find(code: String): PendingCarbonReportBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCarbonReportBudget) {
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

    override fun list(): MutableList<PendingCarbonReportBudget> {
        val out = mutableListOf<PendingCarbonReportBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCarbonReportBudgetStore(): PendingCarbonReportBudgetStore {
    return MemoryPendingCarbonReportBudgetStore(
        mutableMapOf<String, PendingCarbonReportBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCarbonReportBudgetStore(
    var inner: PendingCarbonReportBudgetStore,
    var cache: TtlCache<PendingCarbonReportBudget>,
    var hits: Int,
    var misses: Int,
) : PendingCarbonReportBudgetStore {
    override fun find(code: String): PendingCarbonReportBudget? {
        val hit = (cache.get(code) as PendingCarbonReportBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCarbonReportBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCarbonReportBudget> = inner.list()
}

fun newCachedPendingCarbonReportBudgetStore(inner: PendingCarbonReportBudgetStore, ttl: Int, clock: Clock): PendingCarbonReportBudgetStore {
    return CachedPendingCarbonReportBudgetStore(
        inner = inner,
        cache = newTtlCache<PendingCarbonReportBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending carbon report budgets. Empty fields match all. */
data class PendingCarbonReportBudgetQuery(
    var lane: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCarbonReportBudget): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingCarbonReportBudget>): MutableList<PendingCarbonReportBudget> {
        val matched = filter(items) { candidate: PendingCarbonReportBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCarbonReportBudgets(store: PendingCarbonReportBudgetStore, query: PendingCarbonReportBudgetQuery): MutableList<PendingCarbonReportBudget> {
    return query.apply(store.list())
}
