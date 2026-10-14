package com.acme.transport.freightpay.freightinvoice.bundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice requests by code. */
interface FreightInvoiceRequestStore {
    fun find(code: String): FreightInvoiceRequest?
    fun save(item: FreightInvoiceRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceRequestStore(
    var items: MutableMap<String, FreightInvoiceRequest>,
    var order: MutableList<String>,
) : FreightInvoiceRequestStore {
    override fun find(code: String): FreightInvoiceRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceRequest) {
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

    override fun list(): MutableList<FreightInvoiceRequest> {
        val out = mutableListOf<FreightInvoiceRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceRequestStore(): FreightInvoiceRequestStore {
    return MemoryFreightInvoiceRequestStore(
        mutableMapOf<String, FreightInvoiceRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceRequestStore(
    var inner: FreightInvoiceRequestStore,
    var cache: TtlCache<FreightInvoiceRequest>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceRequestStore {
    override fun find(code: String): FreightInvoiceRequest? {
        val hit = (cache.get(code) as FreightInvoiceRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceRequest> = inner.list()
}

fun newCachedFreightInvoiceRequestStore(inner: FreightInvoiceRequestStore, ttl: Int, clock: Clock): FreightInvoiceRequestStore {
    return CachedFreightInvoiceRequestStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceRequest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice requests. Empty fields match all. */
data class FreightInvoiceRequestQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceRequest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceRequest>): MutableList<FreightInvoiceRequest> {
        val matched = filter(items) { candidate: FreightInvoiceRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceRequests(store: FreightInvoiceRequestStore, query: FreightInvoiceRequestQuery): MutableList<FreightInvoiceRequest> {
    return query.apply(store.list())
}
