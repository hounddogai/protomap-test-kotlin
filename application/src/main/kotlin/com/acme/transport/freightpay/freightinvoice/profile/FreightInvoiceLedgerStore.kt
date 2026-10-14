package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice ledgers by code. */
interface FreightInvoiceLedgerStore {
    fun find(code: String): FreightInvoiceLedger?
    fun save(item: FreightInvoiceLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceLedgerStore(
    var items: MutableMap<String, FreightInvoiceLedger>,
    var order: MutableList<String>,
) : FreightInvoiceLedgerStore {
    override fun find(code: String): FreightInvoiceLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceLedger) {
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

    override fun list(): MutableList<FreightInvoiceLedger> {
        val out = mutableListOf<FreightInvoiceLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceLedgerStore(): FreightInvoiceLedgerStore {
    return MemoryFreightInvoiceLedgerStore(
        mutableMapOf<String, FreightInvoiceLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceLedgerStore(
    var inner: FreightInvoiceLedgerStore,
    var cache: TtlCache<FreightInvoiceLedger>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceLedgerStore {
    override fun find(code: String): FreightInvoiceLedger? {
        val hit = (cache.get(code) as FreightInvoiceLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceLedger> = inner.list()
}

fun newCachedFreightInvoiceLedgerStore(inner: FreightInvoiceLedgerStore, ttl: Int, clock: Clock): FreightInvoiceLedgerStore {
    return CachedFreightInvoiceLedgerStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceLedger>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice ledgers. Empty fields match all. */
data class FreightInvoiceLedgerQuery(
    var depot: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceLedger): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceLedger>): MutableList<FreightInvoiceLedger> {
        val matched = filter(items) { candidate: FreightInvoiceLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceLedgers(store: FreightInvoiceLedgerStore, query: FreightInvoiceLedgerQuery): MutableList<FreightInvoiceLedger> {
    return query.apply(store.list())
}
