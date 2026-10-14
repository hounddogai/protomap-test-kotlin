package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback tariff budgets by code. */
interface FallbackTariffBudgetStore {
    fun find(code: String): FallbackTariffBudget?
    fun save(item: FallbackTariffBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTariffBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTariffBudgetStore(
    var items: MutableMap<String, FallbackTariffBudget>,
    var order: MutableList<String>,
) : FallbackTariffBudgetStore {
    override fun find(code: String): FallbackTariffBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTariffBudget) {
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

    override fun list(): MutableList<FallbackTariffBudget> {
        val out = mutableListOf<FallbackTariffBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTariffBudgetStore(): FallbackTariffBudgetStore {
    return MemoryFallbackTariffBudgetStore(
        mutableMapOf<String, FallbackTariffBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTariffBudgetStore(
    var inner: FallbackTariffBudgetStore,
    var cache: TtlCache<FallbackTariffBudget>,
    var hits: Int,
    var misses: Int,
) : FallbackTariffBudgetStore {
    override fun find(code: String): FallbackTariffBudget? {
        val hit = (cache.get(code) as FallbackTariffBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTariffBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTariffBudget> = inner.list()
}

fun newCachedFallbackTariffBudgetStore(inner: FallbackTariffBudgetStore, ttl: Int, clock: Clock): FallbackTariffBudgetStore {
    return CachedFallbackTariffBudgetStore(
        inner = inner,
        cache = newTtlCache<FallbackTariffBudget>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback tariff budgets. Empty fields match all. */
data class FallbackTariffBudgetQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTariffBudget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<FallbackTariffBudget>): MutableList<FallbackTariffBudget> {
        val matched = filter(items) { candidate: FallbackTariffBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTariffBudgets(store: FallbackTariffBudgetStore, query: FallbackTariffBudgetQuery): MutableList<FallbackTariffBudget> {
    return query.apply(store.list())
}
