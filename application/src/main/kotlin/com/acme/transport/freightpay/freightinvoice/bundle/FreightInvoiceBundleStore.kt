package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice bundles by code. */
interface FreightInvoiceBundleStore {
    fun find(code: String): FreightInvoiceBundle?
    fun save(item: FreightInvoiceBundle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceBundle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceBundleStore(
    var items: MutableMap<String, FreightInvoiceBundle>,
    var order: MutableList<String>,
) : FreightInvoiceBundleStore {
    override fun find(code: String): FreightInvoiceBundle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceBundle) {
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

    override fun list(): MutableList<FreightInvoiceBundle> {
        val out = mutableListOf<FreightInvoiceBundle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceBundleStore(): FreightInvoiceBundleStore {
    return MemoryFreightInvoiceBundleStore(
        mutableMapOf<String, FreightInvoiceBundle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceBundleStore(
    var inner: FreightInvoiceBundleStore,
    var cache: TtlCache<FreightInvoiceBundle>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceBundleStore {
    override fun find(code: String): FreightInvoiceBundle? {
        val hit = (cache.get(code) as FreightInvoiceBundle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceBundle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceBundle> = inner.list()
}

fun newCachedFreightInvoiceBundleStore(inner: FreightInvoiceBundleStore, ttl: Int, clock: Clock): FreightInvoiceBundleStore {
    return CachedFreightInvoiceBundleStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceBundle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice bundles. Empty fields match all. */
data class FreightInvoiceBundleQuery(
    var depot: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceBundle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceBundle>): MutableList<FreightInvoiceBundle> {
        val matched = filter(items) { candidate: FreightInvoiceBundle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceBundles(store: FreightInvoiceBundleStore, query: FreightInvoiceBundleQuery): MutableList<FreightInvoiceBundle> {
    return query.apply(store.list())
}
