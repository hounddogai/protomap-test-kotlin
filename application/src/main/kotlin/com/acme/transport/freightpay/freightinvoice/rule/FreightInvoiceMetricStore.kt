package com.acme.transport.freightpay.freightinvoice.rule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice metrics by code. */
interface FreightInvoiceMetricStore {
    fun find(code: String): FreightInvoiceMetric?
    fun save(item: FreightInvoiceMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceMetricStore(
    var items: MutableMap<String, FreightInvoiceMetric>,
    var order: MutableList<String>,
) : FreightInvoiceMetricStore {
    override fun find(code: String): FreightInvoiceMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceMetric) {
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

    override fun list(): MutableList<FreightInvoiceMetric> {
        val out = mutableListOf<FreightInvoiceMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceMetricStore(): FreightInvoiceMetricStore {
    return MemoryFreightInvoiceMetricStore(
        mutableMapOf<String, FreightInvoiceMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceMetricStore(
    var inner: FreightInvoiceMetricStore,
    var cache: TtlCache<FreightInvoiceMetric>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceMetricStore {
    override fun find(code: String): FreightInvoiceMetric? {
        val hit = (cache.get(code) as FreightInvoiceMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceMetric> = inner.list()
}

fun newCachedFreightInvoiceMetricStore(inner: FreightInvoiceMetricStore, ttl: Int, clock: Clock): FreightInvoiceMetricStore {
    return CachedFreightInvoiceMetricStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice metrics. Empty fields match all. */
data class FreightInvoiceMetricQuery(
    var lane: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceMetric): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceMetric>): MutableList<FreightInvoiceMetric> {
        val matched = filter(items) { candidate: FreightInvoiceMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceMetrics(store: FreightInvoiceMetricStore, query: FreightInvoiceMetricQuery): MutableList<FreightInvoiceMetric> {
    return query.apply(store.list())
}
