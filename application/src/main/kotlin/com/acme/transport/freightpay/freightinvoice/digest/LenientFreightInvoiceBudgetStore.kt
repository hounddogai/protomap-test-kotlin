package com.acme.transport.freightpay.freightinvoice.digest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient freight invoice budgets by code. */
interface LenientFreightInvoiceBudgetStore {
    fun find(code: String): LenientFreightInvoiceBudget?
    fun save(item: LenientFreightInvoiceBudget)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientFreightInvoiceBudget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientFreightInvoiceBudgetStore(
    var items: MutableMap<String, LenientFreightInvoiceBudget>,
    var order: MutableList<String>,
) : LenientFreightInvoiceBudgetStore {
    override fun find(code: String): LenientFreightInvoiceBudget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientFreightInvoiceBudget) {
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

    override fun list(): MutableList<LenientFreightInvoiceBudget> {
        val out = mutableListOf<LenientFreightInvoiceBudget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientFreightInvoiceBudgetStore(): LenientFreightInvoiceBudgetStore {
    return MemoryLenientFreightInvoiceBudgetStore(
        mutableMapOf<String, LenientFreightInvoiceBudget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientFreightInvoiceBudgetStore(
    var inner: LenientFreightInvoiceBudgetStore,
    var cache: TtlCache<LenientFreightInvoiceBudget>,
    var hits: Int,
    var misses: Int,
) : LenientFreightInvoiceBudgetStore {
    override fun find(code: String): LenientFreightInvoiceBudget? {
        val hit = (cache.get(code) as LenientFreightInvoiceBudget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientFreightInvoiceBudget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientFreightInvoiceBudget> = inner.list()
}

fun newCachedLenientFreightInvoiceBudgetStore(inner: LenientFreightInvoiceBudgetStore, ttl: Int, clock: Clock): LenientFreightInvoiceBudgetStore {
    return CachedLenientFreightInvoiceBudgetStore(
        inner = inner,
        cache = newTtlCache<LenientFreightInvoiceBudget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient freight invoice budgets. Empty fields match all. */
data class LenientFreightInvoiceBudgetQuery(
    var unit: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientFreightInvoiceBudget): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientFreightInvoiceBudget>): MutableList<LenientFreightInvoiceBudget> {
        val matched = filter(items) { candidate: LenientFreightInvoiceBudget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientFreightInvoiceBudgets(store: LenientFreightInvoiceBudgetStore, query: LenientFreightInvoiceBudgetQuery): MutableList<LenientFreightInvoiceBudget> {
    return query.apply(store.list())
}
