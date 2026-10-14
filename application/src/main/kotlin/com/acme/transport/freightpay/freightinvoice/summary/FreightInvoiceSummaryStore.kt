package com.acme.transport.freightpay.freightinvoice.summary

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice summaries by code. */
interface FreightInvoiceSummaryStore {
    fun find(code: String): FreightInvoiceSummary?
    fun save(item: FreightInvoiceSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceSummaryStore(
    var items: MutableMap<String, FreightInvoiceSummary>,
    var order: MutableList<String>,
) : FreightInvoiceSummaryStore {
    override fun find(code: String): FreightInvoiceSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceSummary) {
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

    override fun list(): MutableList<FreightInvoiceSummary> {
        val out = mutableListOf<FreightInvoiceSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceSummaryStore(): FreightInvoiceSummaryStore {
    return MemoryFreightInvoiceSummaryStore(
        mutableMapOf<String, FreightInvoiceSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceSummaryStore(
    var inner: FreightInvoiceSummaryStore,
    var cache: TtlCache<FreightInvoiceSummary>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceSummaryStore {
    override fun find(code: String): FreightInvoiceSummary? {
        val hit = (cache.get(code) as FreightInvoiceSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceSummary> = inner.list()
}

fun newCachedFreightInvoiceSummaryStore(inner: FreightInvoiceSummaryStore, ttl: Int, clock: Clock): FreightInvoiceSummaryStore {
    return CachedFreightInvoiceSummaryStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice summaries. Empty fields match all. */
data class FreightInvoiceSummaryQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceSummary): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceSummary>): MutableList<FreightInvoiceSummary> {
        val matched = filter(items) { candidate: FreightInvoiceSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceSummaries(store: FreightInvoiceSummaryStore, query: FreightInvoiceSummaryQuery): MutableList<FreightInvoiceSummary> {
    return query.apply(store.list())
}
