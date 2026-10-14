package com.acme.transport.freightpay.freightinvoice.quota

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice policies by code. */
interface FreightInvoicePolicyStore {
    fun find(code: String): FreightInvoicePolicy?
    fun save(item: FreightInvoicePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoicePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoicePolicyStore(
    var items: MutableMap<String, FreightInvoicePolicy>,
    var order: MutableList<String>,
) : FreightInvoicePolicyStore {
    override fun find(code: String): FreightInvoicePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoicePolicy) {
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

    override fun list(): MutableList<FreightInvoicePolicy> {
        val out = mutableListOf<FreightInvoicePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoicePolicyStore(): FreightInvoicePolicyStore {
    return MemoryFreightInvoicePolicyStore(
        mutableMapOf<String, FreightInvoicePolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoicePolicyStore(
    var inner: FreightInvoicePolicyStore,
    var cache: TtlCache<FreightInvoicePolicy>,
    var hits: Int,
    var misses: Int,
) : FreightInvoicePolicyStore {
    override fun find(code: String): FreightInvoicePolicy? {
        val hit = (cache.get(code) as FreightInvoicePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoicePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoicePolicy> = inner.list()
}

fun newCachedFreightInvoicePolicyStore(inner: FreightInvoicePolicyStore, ttl: Int, clock: Clock): FreightInvoicePolicyStore {
    return CachedFreightInvoicePolicyStore(
        inner = inner,
        cache = newTtlCache<FreightInvoicePolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice policies. Empty fields match all. */
data class FreightInvoicePolicyQuery(
    var label: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoicePolicy): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoicePolicy>): MutableList<FreightInvoicePolicy> {
        val matched = filter(items) { candidate: FreightInvoicePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoicePolicies(store: FreightInvoicePolicyStore, query: FreightInvoicePolicyQuery): MutableList<FreightInvoicePolicy> {
    return query.apply(store.list())
}
