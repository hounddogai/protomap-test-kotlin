package com.acme.transport.fleetops.utilization.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization budgets by code. */
interface UtilizationBudgetStore {
    fun find(code: String): UtilizationBudget?
    fun save(item: UtilizationBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationBudgetStore(
    var items: MutableMap<String, UtilizationBudget>,
    var order: MutableList<String>,
) : UtilizationBudgetStore {
    override fun find(code: String): UtilizationBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationBudget) {
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

    override fun list(): MutableList<UtilizationBudget> {
        val out = mutableListOf<UtilizationBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationBudgetStore(): UtilizationBudgetStore {
    return MemoryUtilizationBudgetStore(mutableMapOf<String, UtilizationBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationBudgetStore(
    var inner: UtilizationBudgetStore,
    var cache: TtlCache<UtilizationBudget>,
    var hits: Int,
    var misses: Int,
) : UtilizationBudgetStore {
    override fun find(code: String): UtilizationBudget? {
        val hit = (cache.get(code) as UtilizationBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationBudget> = inner.list()
}

fun newCachedUtilizationBudgetStore(inner: UtilizationBudgetStore, ttl: Int, clock: Clock): UtilizationBudgetStore {
    return CachedUtilizationBudgetStore(
        inner = inner,
        cache = newTtlCache<UtilizationBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization budgets. Empty fields match all. */
data class UtilizationBudgetQuery(
    var category: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationBudget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationBudget>): MutableList<UtilizationBudget> {
        val matched = filter(items) { candidate: UtilizationBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationBudgets(store: UtilizationBudgetStore, query: UtilizationBudgetQuery): MutableList<UtilizationBudget> {
    return query.apply(store.list())
}
