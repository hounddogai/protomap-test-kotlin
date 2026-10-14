package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending freight invoice summaries by code. */
interface PendingFreightInvoiceSummaryStore {
    fun find(code: String): PendingFreightInvoiceSummary?
    fun save(item: PendingFreightInvoiceSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingFreightInvoiceSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingFreightInvoiceSummaryStore(
    var items: MutableMap<String, PendingFreightInvoiceSummary>,
    var order: MutableList<String>,
) : PendingFreightInvoiceSummaryStore {
    override fun find(code: String): PendingFreightInvoiceSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingFreightInvoiceSummary) {
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

    override fun list(): MutableList<PendingFreightInvoiceSummary> {
        val out = mutableListOf<PendingFreightInvoiceSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingFreightInvoiceSummaryStore(): PendingFreightInvoiceSummaryStore {
    return MemoryPendingFreightInvoiceSummaryStore(
        mutableMapOf<String, PendingFreightInvoiceSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingFreightInvoiceSummaryStore(
    var inner: PendingFreightInvoiceSummaryStore,
    var cache: TtlCache<PendingFreightInvoiceSummary>,
    var hits: Int,
    var misses: Int,
) : PendingFreightInvoiceSummaryStore {
    override fun find(code: String): PendingFreightInvoiceSummary? {
        val hit = (cache.get(code) as PendingFreightInvoiceSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingFreightInvoiceSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingFreightInvoiceSummary> = inner.list()
}

fun newCachedPendingFreightInvoiceSummaryStore(inner: PendingFreightInvoiceSummaryStore, ttl: Int, clock: Clock): PendingFreightInvoiceSummaryStore {
    return CachedPendingFreightInvoiceSummaryStore(
        inner = inner,
        cache = newTtlCache<PendingFreightInvoiceSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending freight invoice summaries. Empty fields match all. */
data class PendingFreightInvoiceSummaryQuery(
    var category: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingFreightInvoiceSummary): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingFreightInvoiceSummary>): MutableList<PendingFreightInvoiceSummary> {
        val matched = filter(items) { candidate: PendingFreightInvoiceSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingFreightInvoiceSummaries(store: PendingFreightInvoiceSummaryStore, query: PendingFreightInvoiceSummaryQuery): MutableList<PendingFreightInvoiceSummary> {
    return query.apply(store.list())
}
