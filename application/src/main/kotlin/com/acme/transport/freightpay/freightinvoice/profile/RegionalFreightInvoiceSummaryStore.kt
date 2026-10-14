package com.acme.transport.freightpay.freightinvoice.profile

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional freight invoice summaries by code. */
interface RegionalFreightInvoiceSummaryStore {
    fun find(code: String): RegionalFreightInvoiceSummary?
    fun save(item: RegionalFreightInvoiceSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFreightInvoiceSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFreightInvoiceSummaryStore(
    var items: MutableMap<String, RegionalFreightInvoiceSummary>,
    var order: MutableList<String>,
) : RegionalFreightInvoiceSummaryStore {
    override fun find(code: String): RegionalFreightInvoiceSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFreightInvoiceSummary) {
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

    override fun list(): MutableList<RegionalFreightInvoiceSummary> {
        val out = mutableListOf<RegionalFreightInvoiceSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFreightInvoiceSummaryStore(): RegionalFreightInvoiceSummaryStore {
    return MemoryRegionalFreightInvoiceSummaryStore(
        mutableMapOf<String, RegionalFreightInvoiceSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFreightInvoiceSummaryStore(
    var inner: RegionalFreightInvoiceSummaryStore,
    var cache: TtlCache<RegionalFreightInvoiceSummary>,
    var hits: Int,
    var misses: Int,
) : RegionalFreightInvoiceSummaryStore {
    override fun find(code: String): RegionalFreightInvoiceSummary? {
        val hit = (cache.get(code) as RegionalFreightInvoiceSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFreightInvoiceSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFreightInvoiceSummary> = inner.list()
}

fun newCachedRegionalFreightInvoiceSummaryStore(inner: RegionalFreightInvoiceSummaryStore, ttl: Int, clock: Clock): RegionalFreightInvoiceSummaryStore {
    return CachedRegionalFreightInvoiceSummaryStore(
        inner = inner,
        cache = newTtlCache<RegionalFreightInvoiceSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional freight invoice summaries. Empty fields match all. */
data class RegionalFreightInvoiceSummaryQuery(
    var source: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFreightInvoiceSummary): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalFreightInvoiceSummary>): MutableList<RegionalFreightInvoiceSummary> {
        val matched = filter(items) { candidate: RegionalFreightInvoiceSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFreightInvoiceSummaries(store: RegionalFreightInvoiceSummaryStore, query: RegionalFreightInvoiceSummaryQuery): MutableList<RegionalFreightInvoiceSummary> {
    return query.apply(store.list())
}
