package com.acme.transport.freightpay.freightinvoice.threshold

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional freight invoice thresholds by code. */
interface RegionalFreightInvoiceThresholdStore {
    fun find(code: String): RegionalFreightInvoiceThreshold?
    fun save(item: RegionalFreightInvoiceThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFreightInvoiceThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFreightInvoiceThresholdStore(
    var items: MutableMap<String, RegionalFreightInvoiceThreshold>,
    var order: MutableList<String>,
) : RegionalFreightInvoiceThresholdStore {
    override fun find(code: String): RegionalFreightInvoiceThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFreightInvoiceThreshold) {
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

    override fun list(): MutableList<RegionalFreightInvoiceThreshold> {
        val out = mutableListOf<RegionalFreightInvoiceThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFreightInvoiceThresholdStore(): RegionalFreightInvoiceThresholdStore {
    return MemoryRegionalFreightInvoiceThresholdStore(
        mutableMapOf<String, RegionalFreightInvoiceThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFreightInvoiceThresholdStore(
    var inner: RegionalFreightInvoiceThresholdStore,
    var cache: TtlCache<RegionalFreightInvoiceThreshold>,
    var hits: Int,
    var misses: Int,
) : RegionalFreightInvoiceThresholdStore {
    override fun find(code: String): RegionalFreightInvoiceThreshold? {
        val hit = (cache.get(code) as RegionalFreightInvoiceThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFreightInvoiceThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFreightInvoiceThreshold> = inner.list()
}

fun newCachedRegionalFreightInvoiceThresholdStore(inner: RegionalFreightInvoiceThresholdStore, ttl: Int, clock: Clock): RegionalFreightInvoiceThresholdStore {
    return CachedRegionalFreightInvoiceThresholdStore(
        inner = inner,
        cache = newTtlCache<RegionalFreightInvoiceThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional freight invoice thresholds. Empty fields match all. */
data class RegionalFreightInvoiceThresholdQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFreightInvoiceThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalFreightInvoiceThreshold>): MutableList<RegionalFreightInvoiceThreshold> {
        val matched = filter(items) { candidate: RegionalFreightInvoiceThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFreightInvoiceThresholds(store: RegionalFreightInvoiceThresholdStore, query: RegionalFreightInvoiceThresholdQuery): MutableList<RegionalFreightInvoiceThreshold> {
    return query.apply(store.list())
}
