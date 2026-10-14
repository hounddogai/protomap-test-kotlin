package com.acme.transport.freightpay.freightinvoice.window

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice thresholds by code. */
interface FreightInvoiceThresholdStore {
    fun find(code: String): FreightInvoiceThreshold?
    fun save(item: FreightInvoiceThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceThresholdStore(
    var items: MutableMap<String, FreightInvoiceThreshold>,
    var order: MutableList<String>,
) : FreightInvoiceThresholdStore {
    override fun find(code: String): FreightInvoiceThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceThreshold) {
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

    override fun list(): MutableList<FreightInvoiceThreshold> {
        val out = mutableListOf<FreightInvoiceThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceThresholdStore(): FreightInvoiceThresholdStore {
    return MemoryFreightInvoiceThresholdStore(
        mutableMapOf<String, FreightInvoiceThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceThresholdStore(
    var inner: FreightInvoiceThresholdStore,
    var cache: TtlCache<FreightInvoiceThreshold>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceThresholdStore {
    override fun find(code: String): FreightInvoiceThreshold? {
        val hit = (cache.get(code) as FreightInvoiceThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceThreshold> = inner.list()
}

fun newCachedFreightInvoiceThresholdStore(inner: FreightInvoiceThresholdStore, ttl: Int, clock: Clock): FreightInvoiceThresholdStore {
    return CachedFreightInvoiceThresholdStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice thresholds. Empty fields match all. */
data class FreightInvoiceThresholdQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<FreightInvoiceThreshold>): MutableList<FreightInvoiceThreshold> {
        val matched = filter(items) { candidate: FreightInvoiceThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceThresholds(store: FreightInvoiceThresholdStore, query: FreightInvoiceThresholdQuery): MutableList<FreightInvoiceThreshold> {
    return query.apply(store.list())
}
