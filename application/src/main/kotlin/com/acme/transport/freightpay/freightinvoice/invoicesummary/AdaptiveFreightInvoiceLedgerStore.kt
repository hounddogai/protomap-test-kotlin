package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive freight invoice ledgers by code. */
interface AdaptiveFreightInvoiceLedgerStore {
    fun find(code: String): AdaptiveFreightInvoiceLedger?
    fun save(item: AdaptiveFreightInvoiceLedger)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveFreightInvoiceLedger>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveFreightInvoiceLedgerStore(
    var items: MutableMap<String, AdaptiveFreightInvoiceLedger>,
    var order: MutableList<String>,
) : AdaptiveFreightInvoiceLedgerStore {
    override fun find(code: String): AdaptiveFreightInvoiceLedger? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveFreightInvoiceLedger) {
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

    override fun list(): MutableList<AdaptiveFreightInvoiceLedger> {
        val out = mutableListOf<AdaptiveFreightInvoiceLedger>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveFreightInvoiceLedgerStore(): AdaptiveFreightInvoiceLedgerStore {
    return MemoryAdaptiveFreightInvoiceLedgerStore(
        mutableMapOf<String, AdaptiveFreightInvoiceLedger>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveFreightInvoiceLedgerStore(
    var inner: AdaptiveFreightInvoiceLedgerStore,
    var cache: TtlCache<AdaptiveFreightInvoiceLedger>,
    var hits: Int,
    var misses: Int,
) : AdaptiveFreightInvoiceLedgerStore {
    override fun find(code: String): AdaptiveFreightInvoiceLedger? {
        val hit = (cache.get(code) as AdaptiveFreightInvoiceLedger?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveFreightInvoiceLedger) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveFreightInvoiceLedger> = inner.list()
}

fun newCachedAdaptiveFreightInvoiceLedgerStore(inner: AdaptiveFreightInvoiceLedgerStore, ttl: Int, clock: Clock): AdaptiveFreightInvoiceLedgerStore {
    return CachedAdaptiveFreightInvoiceLedgerStore(
        inner = inner,
        cache = newTtlCache<AdaptiveFreightInvoiceLedger>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive freight invoice ledgers. Empty fields match all. */
data class AdaptiveFreightInvoiceLedgerQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveFreightInvoiceLedger): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AdaptiveFreightInvoiceLedger>): MutableList<AdaptiveFreightInvoiceLedger> {
        val matched = filter(items) { candidate: AdaptiveFreightInvoiceLedger -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveFreightInvoiceLedgers(store: AdaptiveFreightInvoiceLedgerStore, query: AdaptiveFreightInvoiceLedgerQuery): MutableList<AdaptiveFreightInvoiceLedger> {
    return query.apply(store.list())
}
