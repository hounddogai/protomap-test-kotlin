package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted fuel burn budgets by code. */
interface WeightedFuelBurnBudgetStore {
    fun find(code: String): WeightedFuelBurnBudget?
    fun save(item: WeightedFuelBurnBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedFuelBurnBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedFuelBurnBudgetStore(
    var items: MutableMap<String, WeightedFuelBurnBudget>,
    var order: MutableList<String>,
) : WeightedFuelBurnBudgetStore {
    override fun find(code: String): WeightedFuelBurnBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedFuelBurnBudget) {
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

    override fun list(): MutableList<WeightedFuelBurnBudget> {
        val out = mutableListOf<WeightedFuelBurnBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedFuelBurnBudgetStore(): WeightedFuelBurnBudgetStore {
    return MemoryWeightedFuelBurnBudgetStore(
        mutableMapOf<String, WeightedFuelBurnBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedFuelBurnBudgetStore(
    var inner: WeightedFuelBurnBudgetStore,
    var cache: TtlCache<WeightedFuelBurnBudget>,
    var hits: Int,
    var misses: Int,
) : WeightedFuelBurnBudgetStore {
    override fun find(code: String): WeightedFuelBurnBudget? {
        val hit = (cache.get(code) as WeightedFuelBurnBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedFuelBurnBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedFuelBurnBudget> = inner.list()
}

fun newCachedWeightedFuelBurnBudgetStore(inner: WeightedFuelBurnBudgetStore, ttl: Int, clock: Clock): WeightedFuelBurnBudgetStore {
    return CachedWeightedFuelBurnBudgetStore(
        inner = inner,
        cache = newTtlCache<WeightedFuelBurnBudget>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted fuel burn budgets. Empty fields match all. */
data class WeightedFuelBurnBudgetQuery(
    var locale: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedFuelBurnBudget): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedFuelBurnBudget>): MutableList<WeightedFuelBurnBudget> {
        val matched = filter(items) { candidate: WeightedFuelBurnBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedFuelBurnBudgets(store: WeightedFuelBurnBudgetStore, query: WeightedFuelBurnBudgetQuery): MutableList<WeightedFuelBurnBudget> {
    return query.apply(store.list())
}
