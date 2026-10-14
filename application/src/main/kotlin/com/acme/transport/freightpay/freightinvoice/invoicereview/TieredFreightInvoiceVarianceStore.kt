package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered freight invoice variances by code. */
interface TieredFreightInvoiceVarianceStore {
    fun find(code: String): TieredFreightInvoiceVariance?
    fun save(item: TieredFreightInvoiceVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredFreightInvoiceVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredFreightInvoiceVarianceStore(
    var items: MutableMap<String, TieredFreightInvoiceVariance>,
    var order: MutableList<String>,
) : TieredFreightInvoiceVarianceStore {
    override fun find(code: String): TieredFreightInvoiceVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredFreightInvoiceVariance) {
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

    override fun list(): MutableList<TieredFreightInvoiceVariance> {
        val out = mutableListOf<TieredFreightInvoiceVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredFreightInvoiceVarianceStore(): TieredFreightInvoiceVarianceStore {
    return MemoryTieredFreightInvoiceVarianceStore(
        mutableMapOf<String, TieredFreightInvoiceVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredFreightInvoiceVarianceStore(
    var inner: TieredFreightInvoiceVarianceStore,
    var cache: TtlCache<TieredFreightInvoiceVariance>,
    var hits: Int,
    var misses: Int,
) : TieredFreightInvoiceVarianceStore {
    override fun find(code: String): TieredFreightInvoiceVariance? {
        val hit = (cache.get(code) as TieredFreightInvoiceVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredFreightInvoiceVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredFreightInvoiceVariance> = inner.list()
}

fun newCachedTieredFreightInvoiceVarianceStore(inner: TieredFreightInvoiceVarianceStore, ttl: Int, clock: Clock): TieredFreightInvoiceVarianceStore {
    return CachedTieredFreightInvoiceVarianceStore(
        inner = inner,
        cache = newTtlCache<TieredFreightInvoiceVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered freight invoice variances. Empty fields match all. */
data class TieredFreightInvoiceVarianceQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredFreightInvoiceVariance): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredFreightInvoiceVariance>): MutableList<TieredFreightInvoiceVariance> {
        val matched = filter(items) { candidate: TieredFreightInvoiceVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredFreightInvoiceVariances(store: TieredFreightInvoiceVarianceStore, query: TieredFreightInvoiceVarianceQuery): MutableList<TieredFreightInvoiceVariance> {
    return query.apply(store.list())
}
