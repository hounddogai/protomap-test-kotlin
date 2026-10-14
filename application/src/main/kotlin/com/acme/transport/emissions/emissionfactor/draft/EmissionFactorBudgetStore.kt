package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor budgets by code. */
interface EmissionFactorBudgetStore {
    fun find(code: String): EmissionFactorBudget?
    fun save(item: EmissionFactorBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorBudgetStore(
    var items: MutableMap<String, EmissionFactorBudget>,
    var order: MutableList<String>,
) : EmissionFactorBudgetStore {
    override fun find(code: String): EmissionFactorBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorBudget) {
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

    override fun list(): MutableList<EmissionFactorBudget> {
        val out = mutableListOf<EmissionFactorBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorBudgetStore(): EmissionFactorBudgetStore {
    return MemoryEmissionFactorBudgetStore(
        mutableMapOf<String, EmissionFactorBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorBudgetStore(
    var inner: EmissionFactorBudgetStore,
    var cache: TtlCache<EmissionFactorBudget>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorBudgetStore {
    override fun find(code: String): EmissionFactorBudget? {
        val hit = (cache.get(code) as EmissionFactorBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorBudget> = inner.list()
}

fun newCachedEmissionFactorBudgetStore(inner: EmissionFactorBudgetStore, ttl: Int, clock: Clock): EmissionFactorBudgetStore {
    return CachedEmissionFactorBudgetStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorBudget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor budgets. Empty fields match all. */
data class EmissionFactorBudgetQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorBudget): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorBudget>): MutableList<EmissionFactorBudget> {
        val matched = filter(items) { candidate: EmissionFactorBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorBudgets(store: EmissionFactorBudgetStore, query: EmissionFactorBudgetQuery): MutableList<EmissionFactorBudget> {
    return query.apply(store.list())
}
