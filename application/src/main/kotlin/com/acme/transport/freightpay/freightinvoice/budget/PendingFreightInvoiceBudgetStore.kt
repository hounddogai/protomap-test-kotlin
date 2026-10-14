package com.acme.transport.freightpay.freightinvoice.budget

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending freight invoice budgets by code. */
interface PendingFreightInvoiceBudgetStore {
    fun find(code: String): PendingFreightInvoiceBudget?
    fun save(item: PendingFreightInvoiceBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingFreightInvoiceBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingFreightInvoiceBudgetStore(
    var items: MutableMap<String, PendingFreightInvoiceBudget>,
    var order: MutableList<String>,
) : PendingFreightInvoiceBudgetStore {
    override fun find(code: String): PendingFreightInvoiceBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingFreightInvoiceBudget) {
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

    override fun list(): MutableList<PendingFreightInvoiceBudget> {
        val out = mutableListOf<PendingFreightInvoiceBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingFreightInvoiceBudgetStore(): PendingFreightInvoiceBudgetStore {
    return MemoryPendingFreightInvoiceBudgetStore(
        mutableMapOf<String, PendingFreightInvoiceBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingFreightInvoiceBudgetStore(
    var inner: PendingFreightInvoiceBudgetStore,
    var cache: TtlCache<PendingFreightInvoiceBudget>,
    var hits: Int,
    var misses: Int,
) : PendingFreightInvoiceBudgetStore {
    override fun find(code: String): PendingFreightInvoiceBudget? {
        val hit = (cache.get(code) as PendingFreightInvoiceBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingFreightInvoiceBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingFreightInvoiceBudget> = inner.list()
}

fun newCachedPendingFreightInvoiceBudgetStore(inner: PendingFreightInvoiceBudgetStore, ttl: Int, clock: Clock): PendingFreightInvoiceBudgetStore {
    return CachedPendingFreightInvoiceBudgetStore(
        inner = inner,
        cache = newTtlCache<PendingFreightInvoiceBudget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending freight invoice budgets. Empty fields match all. */
data class PendingFreightInvoiceBudgetQuery(
    var country: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingFreightInvoiceBudget): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingFreightInvoiceBudget>): MutableList<PendingFreightInvoiceBudget> {
        val matched = filter(items) { candidate: PendingFreightInvoiceBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingFreightInvoiceBudgets(store: PendingFreightInvoiceBudgetStore, query: PendingFreightInvoiceBudgetQuery): MutableList<PendingFreightInvoiceBudget> {
    return query.apply(store.list())
}
