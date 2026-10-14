package com.acme.transport.freightpay.freightinvoice.invoicerollup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global freight invoice rollups by code. */
interface GlobalFreightInvoiceRollupStore {
    fun find(code: String): GlobalFreightInvoiceRollup?
    fun save(item: GlobalFreightInvoiceRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalFreightInvoiceRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalFreightInvoiceRollupStore(
    var items: MutableMap<String, GlobalFreightInvoiceRollup>,
    var order: MutableList<String>,
) : GlobalFreightInvoiceRollupStore {
    override fun find(code: String): GlobalFreightInvoiceRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalFreightInvoiceRollup) {
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

    override fun list(): MutableList<GlobalFreightInvoiceRollup> {
        val out = mutableListOf<GlobalFreightInvoiceRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalFreightInvoiceRollupStore(): GlobalFreightInvoiceRollupStore {
    return MemoryGlobalFreightInvoiceRollupStore(
        mutableMapOf<String, GlobalFreightInvoiceRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalFreightInvoiceRollupStore(
    var inner: GlobalFreightInvoiceRollupStore,
    var cache: TtlCache<GlobalFreightInvoiceRollup>,
    var hits: Int,
    var misses: Int,
) : GlobalFreightInvoiceRollupStore {
    override fun find(code: String): GlobalFreightInvoiceRollup? {
        val hit = (cache.get(code) as GlobalFreightInvoiceRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalFreightInvoiceRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalFreightInvoiceRollup> = inner.list()
}

fun newCachedGlobalFreightInvoiceRollupStore(inner: GlobalFreightInvoiceRollupStore, ttl: Int, clock: Clock): GlobalFreightInvoiceRollupStore {
    return CachedGlobalFreightInvoiceRollupStore(
        inner = inner,
        cache = newTtlCache<GlobalFreightInvoiceRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global freight invoice rollups. Empty fields match all. */
data class GlobalFreightInvoiceRollupQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalFreightInvoiceRollup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<GlobalFreightInvoiceRollup>): MutableList<GlobalFreightInvoiceRollup> {
        val matched = filter(items) { candidate: GlobalFreightInvoiceRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalFreightInvoiceRollups(store: GlobalFreightInvoiceRollupStore, query: GlobalFreightInvoiceRollupQuery): MutableList<GlobalFreightInvoiceRollup> {
    return query.apply(store.list())
}
