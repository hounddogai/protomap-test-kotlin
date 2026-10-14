package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted freight invoice budgets by code. */
interface WeightedFreightInvoiceBudgetStore {
    fun find(code: String): WeightedFreightInvoiceBudget?
    fun save(item: WeightedFreightInvoiceBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedFreightInvoiceBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedFreightInvoiceBudgetStore(
    var items: MutableMap<String, WeightedFreightInvoiceBudget>,
    var order: MutableList<String>,
) : WeightedFreightInvoiceBudgetStore {
    override fun find(code: String): WeightedFreightInvoiceBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedFreightInvoiceBudget) {
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

    override fun list(): MutableList<WeightedFreightInvoiceBudget> {
        val out = mutableListOf<WeightedFreightInvoiceBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedFreightInvoiceBudgetStore(): WeightedFreightInvoiceBudgetStore {
    return MemoryWeightedFreightInvoiceBudgetStore(
        mutableMapOf<String, WeightedFreightInvoiceBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedFreightInvoiceBudgetStore(
    var inner: WeightedFreightInvoiceBudgetStore,
    var cache: TtlCache<WeightedFreightInvoiceBudget>,
    var hits: Int,
    var misses: Int,
) : WeightedFreightInvoiceBudgetStore {
    override fun find(code: String): WeightedFreightInvoiceBudget? {
        val hit = (cache.get(code) as WeightedFreightInvoiceBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedFreightInvoiceBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedFreightInvoiceBudget> = inner.list()
}

fun newCachedWeightedFreightInvoiceBudgetStore(inner: WeightedFreightInvoiceBudgetStore, ttl: Int, clock: Clock): WeightedFreightInvoiceBudgetStore {
    return CachedWeightedFreightInvoiceBudgetStore(
        inner = inner,
        cache = newTtlCache<WeightedFreightInvoiceBudget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted freight invoice budgets. Empty fields match all. */
data class WeightedFreightInvoiceBudgetQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedFreightInvoiceBudget): Boolean {
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

    fun apply(items: MutableList<WeightedFreightInvoiceBudget>): MutableList<WeightedFreightInvoiceBudget> {
        val matched = filter(items) { candidate: WeightedFreightInvoiceBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedFreightInvoiceBudgets(store: WeightedFreightInvoiceBudgetStore, query: WeightedFreightInvoiceBudgetQuery): MutableList<WeightedFreightInvoiceBudget> {
    return query.apply(store.list())
}
