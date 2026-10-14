package com.acme.transport.emissions.fuelburn.burnbudget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped fuel burn budgets by code. */
interface CappedFuelBurnBudgetStore {
    fun find(code: String): CappedFuelBurnBudget?
    fun save(item: CappedFuelBurnBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedFuelBurnBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedFuelBurnBudgetStore(
    var items: MutableMap<String, CappedFuelBurnBudget>,
    var order: MutableList<String>,
) : CappedFuelBurnBudgetStore {
    override fun find(code: String): CappedFuelBurnBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedFuelBurnBudget) {
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

    override fun list(): MutableList<CappedFuelBurnBudget> {
        val out = mutableListOf<CappedFuelBurnBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedFuelBurnBudgetStore(): CappedFuelBurnBudgetStore {
    return MemoryCappedFuelBurnBudgetStore(
        mutableMapOf<String, CappedFuelBurnBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedFuelBurnBudgetStore(
    var inner: CappedFuelBurnBudgetStore,
    var cache: TtlCache<CappedFuelBurnBudget>,
    var hits: Int,
    var misses: Int,
) : CappedFuelBurnBudgetStore {
    override fun find(code: String): CappedFuelBurnBudget? {
        val hit = (cache.get(code) as CappedFuelBurnBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedFuelBurnBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedFuelBurnBudget> = inner.list()
}

fun newCachedCappedFuelBurnBudgetStore(inner: CappedFuelBurnBudgetStore, ttl: Int, clock: Clock): CappedFuelBurnBudgetStore {
    return CachedCappedFuelBurnBudgetStore(
        inner = inner,
        cache = newTtlCache<CappedFuelBurnBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped fuel burn budgets. Empty fields match all. */
data class CappedFuelBurnBudgetQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedFuelBurnBudget): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<CappedFuelBurnBudget>): MutableList<CappedFuelBurnBudget> {
        val matched = filter(items) { candidate: CappedFuelBurnBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedFuelBurnBudgets(store: CappedFuelBurnBudgetStore, query: CappedFuelBurnBudgetQuery): MutableList<CappedFuelBurnBudget> {
    return query.apply(store.list())
}
