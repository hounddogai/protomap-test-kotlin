package com.acme.transport.freightpay.freightinvoice.proposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict freight invoice requests by code. */
interface StrictFreightInvoiceRequestStore {
    fun find(code: String): StrictFreightInvoiceRequest?
    fun save(item: StrictFreightInvoiceRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictFreightInvoiceRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictFreightInvoiceRequestStore(
    var items: MutableMap<String, StrictFreightInvoiceRequest>,
    var order: MutableList<String>,
) : StrictFreightInvoiceRequestStore {
    override fun find(code: String): StrictFreightInvoiceRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictFreightInvoiceRequest) {
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

    override fun list(): MutableList<StrictFreightInvoiceRequest> {
        val out = mutableListOf<StrictFreightInvoiceRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictFreightInvoiceRequestStore(): StrictFreightInvoiceRequestStore {
    return MemoryStrictFreightInvoiceRequestStore(
        mutableMapOf<String, StrictFreightInvoiceRequest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictFreightInvoiceRequestStore(
    var inner: StrictFreightInvoiceRequestStore,
    var cache: TtlCache<StrictFreightInvoiceRequest>,
    var hits: Int,
    var misses: Int,
) : StrictFreightInvoiceRequestStore {
    override fun find(code: String): StrictFreightInvoiceRequest? {
        val hit = (cache.get(code) as StrictFreightInvoiceRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictFreightInvoiceRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictFreightInvoiceRequest> = inner.list()
}

fun newCachedStrictFreightInvoiceRequestStore(inner: StrictFreightInvoiceRequestStore, ttl: Int, clock: Clock): StrictFreightInvoiceRequestStore {
    return CachedStrictFreightInvoiceRequestStore(
        inner = inner,
        cache = newTtlCache<StrictFreightInvoiceRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict freight invoice requests. Empty fields match all. */
data class StrictFreightInvoiceRequestQuery(
    var unit: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictFreightInvoiceRequest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictFreightInvoiceRequest>): MutableList<StrictFreightInvoiceRequest> {
        val matched = filter(items) { candidate: StrictFreightInvoiceRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictFreightInvoiceRequests(store: StrictFreightInvoiceRequestStore, query: StrictFreightInvoiceRequestQuery): MutableList<StrictFreightInvoiceRequest> {
    return query.apply(store.list())
}
