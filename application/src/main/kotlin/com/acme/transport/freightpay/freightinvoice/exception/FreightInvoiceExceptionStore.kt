package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice exceptions by code. */
interface FreightInvoiceExceptionStore {
    fun find(code: String): FreightInvoiceException?
    fun save(item: FreightInvoiceException)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceExceptionStore(
    var items: MutableMap<String, FreightInvoiceException>,
    var order: MutableList<String>,
) : FreightInvoiceExceptionStore {
    override fun find(code: String): FreightInvoiceException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceException) {
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

    override fun list(): MutableList<FreightInvoiceException> {
        val out = mutableListOf<FreightInvoiceException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceExceptionStore(): FreightInvoiceExceptionStore {
    return MemoryFreightInvoiceExceptionStore(
        mutableMapOf<String, FreightInvoiceException>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceExceptionStore(
    var inner: FreightInvoiceExceptionStore,
    var cache: TtlCache<FreightInvoiceException>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceExceptionStore {
    override fun find(code: String): FreightInvoiceException? {
        val hit = (cache.get(code) as FreightInvoiceException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceException> = inner.list()
}

fun newCachedFreightInvoiceExceptionStore(inner: FreightInvoiceExceptionStore, ttl: Int, clock: Clock): FreightInvoiceExceptionStore {
    return CachedFreightInvoiceExceptionStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceException>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice exceptions. Empty fields match all. */
data class FreightInvoiceExceptionQuery(
    var lane: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceException): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceException>): MutableList<FreightInvoiceException> {
        val matched = filter(items) { candidate: FreightInvoiceException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceExceptions(store: FreightInvoiceExceptionStore, query: FreightInvoiceExceptionQuery): MutableList<FreightInvoiceException> {
    return query.apply(store.list())
}
