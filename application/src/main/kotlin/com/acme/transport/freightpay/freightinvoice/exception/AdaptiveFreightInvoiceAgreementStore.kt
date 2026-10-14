package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive freight invoice agreements by code. */
interface AdaptiveFreightInvoiceAgreementStore {
    fun find(code: String): AdaptiveFreightInvoiceAgreement?
    fun save(item: AdaptiveFreightInvoiceAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveFreightInvoiceAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveFreightInvoiceAgreementStore(
    var items: MutableMap<String, AdaptiveFreightInvoiceAgreement>,
    var order: MutableList<String>,
) : AdaptiveFreightInvoiceAgreementStore {
    override fun find(code: String): AdaptiveFreightInvoiceAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveFreightInvoiceAgreement) {
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

    override fun list(): MutableList<AdaptiveFreightInvoiceAgreement> {
        val out = mutableListOf<AdaptiveFreightInvoiceAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveFreightInvoiceAgreementStore(): AdaptiveFreightInvoiceAgreementStore {
    return MemoryAdaptiveFreightInvoiceAgreementStore(
        mutableMapOf<String, AdaptiveFreightInvoiceAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveFreightInvoiceAgreementStore(
    var inner: AdaptiveFreightInvoiceAgreementStore,
    var cache: TtlCache<AdaptiveFreightInvoiceAgreement>,
    var hits: Int,
    var misses: Int,
) : AdaptiveFreightInvoiceAgreementStore {
    override fun find(code: String): AdaptiveFreightInvoiceAgreement? {
        val hit = (cache.get(code) as AdaptiveFreightInvoiceAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveFreightInvoiceAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveFreightInvoiceAgreement> = inner.list()
}

fun newCachedAdaptiveFreightInvoiceAgreementStore(inner: AdaptiveFreightInvoiceAgreementStore, ttl: Int, clock: Clock): AdaptiveFreightInvoiceAgreementStore {
    return CachedAdaptiveFreightInvoiceAgreementStore(
        inner = inner,
        cache = newTtlCache<AdaptiveFreightInvoiceAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive freight invoice agreements. Empty fields match all. */
data class AdaptiveFreightInvoiceAgreementQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveFreightInvoiceAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveFreightInvoiceAgreement>): MutableList<AdaptiveFreightInvoiceAgreement> {
        val matched = filter(items) { candidate: AdaptiveFreightInvoiceAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveFreightInvoiceAgreements(store: AdaptiveFreightInvoiceAgreementStore, query: AdaptiveFreightInvoiceAgreementQuery): MutableList<AdaptiveFreightInvoiceAgreement> {
    return query.apply(store.list())
}
