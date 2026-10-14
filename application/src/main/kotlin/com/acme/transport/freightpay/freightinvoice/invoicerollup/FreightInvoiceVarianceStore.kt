package com.acme.transport.freightpay.freightinvoice.invoicerollup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice variances by code. */
interface FreightInvoiceVarianceStore {
    fun find(code: String): FreightInvoiceVariance?
    fun save(item: FreightInvoiceVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceVarianceStore(
    var items: MutableMap<String, FreightInvoiceVariance>,
    var order: MutableList<String>,
) : FreightInvoiceVarianceStore {
    override fun find(code: String): FreightInvoiceVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceVariance) {
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

    override fun list(): MutableList<FreightInvoiceVariance> {
        val out = mutableListOf<FreightInvoiceVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceVarianceStore(): FreightInvoiceVarianceStore {
    return MemoryFreightInvoiceVarianceStore(
        mutableMapOf<String, FreightInvoiceVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceVarianceStore(
    var inner: FreightInvoiceVarianceStore,
    var cache: TtlCache<FreightInvoiceVariance>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceVarianceStore {
    override fun find(code: String): FreightInvoiceVariance? {
        val hit = (cache.get(code) as FreightInvoiceVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceVariance> = inner.list()
}

fun newCachedFreightInvoiceVarianceStore(inner: FreightInvoiceVarianceStore, ttl: Int, clock: Clock): FreightInvoiceVarianceStore {
    return CachedFreightInvoiceVarianceStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice variances. Empty fields match all. */
data class FreightInvoiceVarianceQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceVariance): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceVariance>): MutableList<FreightInvoiceVariance> {
        val matched = filter(items) { candidate: FreightInvoiceVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceVariances(store: FreightInvoiceVarianceStore, query: FreightInvoiceVarianceQuery): MutableList<FreightInvoiceVariance> {
    return query.apply(store.list())
}
