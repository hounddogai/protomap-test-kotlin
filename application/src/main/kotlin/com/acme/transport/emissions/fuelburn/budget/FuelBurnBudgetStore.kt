package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn budgets by code. */
interface FuelBurnBudgetStore {
    fun find(code: String): FuelBurnBudget?
    fun save(item: FuelBurnBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnBudgetStore(
    var items: MutableMap<String, FuelBurnBudget>,
    var order: MutableList<String>,
) : FuelBurnBudgetStore {
    override fun find(code: String): FuelBurnBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnBudget) {
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

    override fun list(): MutableList<FuelBurnBudget> {
        val out = mutableListOf<FuelBurnBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnBudgetStore(): FuelBurnBudgetStore {
    return MemoryFuelBurnBudgetStore(mutableMapOf<String, FuelBurnBudget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnBudgetStore(
    var inner: FuelBurnBudgetStore,
    var cache: TtlCache<FuelBurnBudget>,
    var hits: Int,
    var misses: Int,
) : FuelBurnBudgetStore {
    override fun find(code: String): FuelBurnBudget? {
        val hit = (cache.get(code) as FuelBurnBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnBudget> = inner.list()
}

fun newCachedFuelBurnBudgetStore(inner: FuelBurnBudgetStore, ttl: Int, clock: Clock): FuelBurnBudgetStore {
    return CachedFuelBurnBudgetStore(
        inner = inner,
        cache = newTtlCache<FuelBurnBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn budgets. Empty fields match all. */
data class FuelBurnBudgetQuery(
    var region: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnBudget): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<FuelBurnBudget>): MutableList<FuelBurnBudget> {
        val matched = filter(items) { candidate: FuelBurnBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnBudgets(store: FuelBurnBudgetStore, query: FuelBurnBudgetQuery): MutableList<FuelBurnBudget> {
    return query.apply(store.list())
}
