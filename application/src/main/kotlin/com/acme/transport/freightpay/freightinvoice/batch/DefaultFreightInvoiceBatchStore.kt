package com.acme.transport.freightpay.freightinvoice.batch

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default freight invoice batches by code. */
interface DefaultFreightInvoiceBatchStore {
    fun find(code: String): DefaultFreightInvoiceBatch?
    fun save(item: DefaultFreightInvoiceBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultFreightInvoiceBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultFreightInvoiceBatchStore(
    var items: MutableMap<String, DefaultFreightInvoiceBatch>,
    var order: MutableList<String>,
) : DefaultFreightInvoiceBatchStore {
    override fun find(code: String): DefaultFreightInvoiceBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultFreightInvoiceBatch) {
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

    override fun list(): MutableList<DefaultFreightInvoiceBatch> {
        val out = mutableListOf<DefaultFreightInvoiceBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultFreightInvoiceBatchStore(): DefaultFreightInvoiceBatchStore {
    return MemoryDefaultFreightInvoiceBatchStore(
        mutableMapOf<String, DefaultFreightInvoiceBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultFreightInvoiceBatchStore(
    var inner: DefaultFreightInvoiceBatchStore,
    var cache: TtlCache<DefaultFreightInvoiceBatch>,
    var hits: Int,
    var misses: Int,
) : DefaultFreightInvoiceBatchStore {
    override fun find(code: String): DefaultFreightInvoiceBatch? {
        val hit = (cache.get(code) as DefaultFreightInvoiceBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultFreightInvoiceBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultFreightInvoiceBatch> = inner.list()
}

fun newCachedDefaultFreightInvoiceBatchStore(inner: DefaultFreightInvoiceBatchStore, ttl: Int, clock: Clock): DefaultFreightInvoiceBatchStore {
    return CachedDefaultFreightInvoiceBatchStore(
        inner = inner,
        cache = newTtlCache<DefaultFreightInvoiceBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default freight invoice batches. Empty fields match all. */
data class DefaultFreightInvoiceBatchQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultFreightInvoiceBatch): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<DefaultFreightInvoiceBatch>): MutableList<DefaultFreightInvoiceBatch> {
        val matched = filter(items) { candidate: DefaultFreightInvoiceBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultFreightInvoiceBatches(store: DefaultFreightInvoiceBatchStore, query: DefaultFreightInvoiceBatchQuery): MutableList<DefaultFreightInvoiceBatch> {
    return query.apply(store.list())
}
