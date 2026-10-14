package com.acme.transport.freightpay.freightinvoice.invoicequota

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive freight invoice summaries by code. */
interface AdaptiveFreightInvoiceSummaryStore {
    fun find(code: String): AdaptiveFreightInvoiceSummary?
    fun save(item: AdaptiveFreightInvoiceSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveFreightInvoiceSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveFreightInvoiceSummaryStore(
    var items: MutableMap<String, AdaptiveFreightInvoiceSummary>,
    var order: MutableList<String>,
) : AdaptiveFreightInvoiceSummaryStore {
    override fun find(code: String): AdaptiveFreightInvoiceSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveFreightInvoiceSummary) {
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

    override fun list(): MutableList<AdaptiveFreightInvoiceSummary> {
        val out = mutableListOf<AdaptiveFreightInvoiceSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveFreightInvoiceSummaryStore(): AdaptiveFreightInvoiceSummaryStore {
    return MemoryAdaptiveFreightInvoiceSummaryStore(
        mutableMapOf<String, AdaptiveFreightInvoiceSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveFreightInvoiceSummaryStore(
    var inner: AdaptiveFreightInvoiceSummaryStore,
    var cache: TtlCache<AdaptiveFreightInvoiceSummary>,
    var hits: Int,
    var misses: Int,
) : AdaptiveFreightInvoiceSummaryStore {
    override fun find(code: String): AdaptiveFreightInvoiceSummary? {
        val hit = (cache.get(code) as AdaptiveFreightInvoiceSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveFreightInvoiceSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveFreightInvoiceSummary> = inner.list()
}

fun newCachedAdaptiveFreightInvoiceSummaryStore(inner: AdaptiveFreightInvoiceSummaryStore, ttl: Int, clock: Clock): AdaptiveFreightInvoiceSummaryStore {
    return CachedAdaptiveFreightInvoiceSummaryStore(
        inner = inner,
        cache = newTtlCache<AdaptiveFreightInvoiceSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive freight invoice summaries. Empty fields match all. */
data class AdaptiveFreightInvoiceSummaryQuery(
    var source: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveFreightInvoiceSummary): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<AdaptiveFreightInvoiceSummary>): MutableList<AdaptiveFreightInvoiceSummary> {
        val matched = filter(items) { candidate: AdaptiveFreightInvoiceSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveFreightInvoiceSummaries(store: AdaptiveFreightInvoiceSummaryStore, query: AdaptiveFreightInvoiceSummaryQuery): MutableList<AdaptiveFreightInvoiceSummary> {
    return query.apply(store.list())
}
