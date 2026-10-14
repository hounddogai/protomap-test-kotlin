package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice agreements by code. */
interface FreightInvoiceAgreementStore {
    fun find(code: String): FreightInvoiceAgreement?
    fun save(item: FreightInvoiceAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceAgreementStore(
    var items: MutableMap<String, FreightInvoiceAgreement>,
    var order: MutableList<String>,
) : FreightInvoiceAgreementStore {
    override fun find(code: String): FreightInvoiceAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceAgreement) {
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

    override fun list(): MutableList<FreightInvoiceAgreement> {
        val out = mutableListOf<FreightInvoiceAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceAgreementStore(): FreightInvoiceAgreementStore {
    return MemoryFreightInvoiceAgreementStore(
        mutableMapOf<String, FreightInvoiceAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceAgreementStore(
    var inner: FreightInvoiceAgreementStore,
    var cache: TtlCache<FreightInvoiceAgreement>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceAgreementStore {
    override fun find(code: String): FreightInvoiceAgreement? {
        val hit = (cache.get(code) as FreightInvoiceAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceAgreement> = inner.list()
}

fun newCachedFreightInvoiceAgreementStore(inner: FreightInvoiceAgreementStore, ttl: Int, clock: Clock): FreightInvoiceAgreementStore {
    return CachedFreightInvoiceAgreementStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice agreements. Empty fields match all. */
data class FreightInvoiceAgreementQuery(
    var category: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceAgreement): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceAgreement>): MutableList<FreightInvoiceAgreement> {
        val matched = filter(items) { candidate: FreightInvoiceAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceAgreements(store: FreightInvoiceAgreementStore, query: FreightInvoiceAgreementQuery): MutableList<FreightInvoiceAgreement> {
    return query.apply(store.list())
}
