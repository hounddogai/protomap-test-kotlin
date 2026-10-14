package com.acme.transport.emissions.fuelburn.agreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient fuel burn budgets by code. */
interface LenientFuelBurnBudgetStore {
    fun find(code: String): LenientFuelBurnBudget?
    fun save(item: LenientFuelBurnBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientFuelBurnBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientFuelBurnBudgetStore(
    var items: MutableMap<String, LenientFuelBurnBudget>,
    var order: MutableList<String>,
) : LenientFuelBurnBudgetStore {
    override fun find(code: String): LenientFuelBurnBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientFuelBurnBudget) {
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

    override fun list(): MutableList<LenientFuelBurnBudget> {
        val out = mutableListOf<LenientFuelBurnBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientFuelBurnBudgetStore(): LenientFuelBurnBudgetStore {
    return MemoryLenientFuelBurnBudgetStore(
        mutableMapOf<String, LenientFuelBurnBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientFuelBurnBudgetStore(
    var inner: LenientFuelBurnBudgetStore,
    var cache: TtlCache<LenientFuelBurnBudget>,
    var hits: Int,
    var misses: Int,
) : LenientFuelBurnBudgetStore {
    override fun find(code: String): LenientFuelBurnBudget? {
        val hit = (cache.get(code) as LenientFuelBurnBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientFuelBurnBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientFuelBurnBudget> = inner.list()
}

fun newCachedLenientFuelBurnBudgetStore(inner: LenientFuelBurnBudgetStore, ttl: Int, clock: Clock): LenientFuelBurnBudgetStore {
    return CachedLenientFuelBurnBudgetStore(
        inner = inner,
        cache = newTtlCache<LenientFuelBurnBudget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient fuel burn budgets. Empty fields match all. */
data class LenientFuelBurnBudgetQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientFuelBurnBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LenientFuelBurnBudget>): MutableList<LenientFuelBurnBudget> {
        val matched = filter(items) { candidate: LenientFuelBurnBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientFuelBurnBudgets(store: LenientFuelBurnBudgetStore, query: LenientFuelBurnBudgetQuery): MutableList<LenientFuelBurnBudget> {
    return query.apply(store.list())
}
