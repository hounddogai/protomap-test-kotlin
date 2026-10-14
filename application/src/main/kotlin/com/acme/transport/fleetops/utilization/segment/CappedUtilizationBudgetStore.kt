package com.acme.transport.fleetops.utilization.segment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped utilization budgets by code. */
interface CappedUtilizationBudgetStore {
    fun find(code: String): CappedUtilizationBudget?
    fun save(item: CappedUtilizationBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedUtilizationBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedUtilizationBudgetStore(
    var items: MutableMap<String, CappedUtilizationBudget>,
    var order: MutableList<String>,
) : CappedUtilizationBudgetStore {
    override fun find(code: String): CappedUtilizationBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedUtilizationBudget) {
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

    override fun list(): MutableList<CappedUtilizationBudget> {
        val out = mutableListOf<CappedUtilizationBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedUtilizationBudgetStore(): CappedUtilizationBudgetStore {
    return MemoryCappedUtilizationBudgetStore(
        mutableMapOf<String, CappedUtilizationBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedUtilizationBudgetStore(
    var inner: CappedUtilizationBudgetStore,
    var cache: TtlCache<CappedUtilizationBudget>,
    var hits: Int,
    var misses: Int,
) : CappedUtilizationBudgetStore {
    override fun find(code: String): CappedUtilizationBudget? {
        val hit = (cache.get(code) as CappedUtilizationBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedUtilizationBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedUtilizationBudget> = inner.list()
}

fun newCachedCappedUtilizationBudgetStore(inner: CappedUtilizationBudgetStore, ttl: Int, clock: Clock): CappedUtilizationBudgetStore {
    return CachedCappedUtilizationBudgetStore(
        inner = inner,
        cache = newTtlCache<CappedUtilizationBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped utilization budgets. Empty fields match all. */
data class CappedUtilizationBudgetQuery(
    var category: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedUtilizationBudget): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedUtilizationBudget>): MutableList<CappedUtilizationBudget> {
        val matched = filter(items) { candidate: CappedUtilizationBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedUtilizationBudgets(store: CappedUtilizationBudgetStore, query: CappedUtilizationBudgetQuery): MutableList<CappedUtilizationBudget> {
    return query.apply(store.list())
}
