package com.acme.transport.freightpay.freightinvoice.invoiceadjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary freight invoice adjustments by code. */
interface PrimaryFreightInvoiceAdjustmentStore {
    fun find(code: String): PrimaryFreightInvoiceAdjustment?
    fun save(item: PrimaryFreightInvoiceAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryFreightInvoiceAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryFreightInvoiceAdjustmentStore(
    var items: MutableMap<String, PrimaryFreightInvoiceAdjustment>,
    var order: MutableList<String>,
) : PrimaryFreightInvoiceAdjustmentStore {
    override fun find(code: String): PrimaryFreightInvoiceAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryFreightInvoiceAdjustment) {
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

    override fun list(): MutableList<PrimaryFreightInvoiceAdjustment> {
        val out = mutableListOf<PrimaryFreightInvoiceAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryFreightInvoiceAdjustmentStore(): PrimaryFreightInvoiceAdjustmentStore {
    return MemoryPrimaryFreightInvoiceAdjustmentStore(
        mutableMapOf<String, PrimaryFreightInvoiceAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryFreightInvoiceAdjustmentStore(
    var inner: PrimaryFreightInvoiceAdjustmentStore,
    var cache: TtlCache<PrimaryFreightInvoiceAdjustment>,
    var hits: Int,
    var misses: Int,
) : PrimaryFreightInvoiceAdjustmentStore {
    override fun find(code: String): PrimaryFreightInvoiceAdjustment? {
        val hit = (cache.get(code) as PrimaryFreightInvoiceAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryFreightInvoiceAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryFreightInvoiceAdjustment> = inner.list()
}

fun newCachedPrimaryFreightInvoiceAdjustmentStore(inner: PrimaryFreightInvoiceAdjustmentStore, ttl: Int, clock: Clock): PrimaryFreightInvoiceAdjustmentStore {
    return CachedPrimaryFreightInvoiceAdjustmentStore(
        inner = inner,
        cache = newTtlCache<PrimaryFreightInvoiceAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary freight invoice adjustments. Empty fields match all. */
data class PrimaryFreightInvoiceAdjustmentQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryFreightInvoiceAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryFreightInvoiceAdjustment>): MutableList<PrimaryFreightInvoiceAdjustment> {
        val matched = filter(items) { candidate: PrimaryFreightInvoiceAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryFreightInvoiceAdjustments(store: PrimaryFreightInvoiceAdjustmentStore, query: PrimaryFreightInvoiceAdjustmentQuery): MutableList<PrimaryFreightInvoiceAdjustment> {
    return query.apply(store.list())
}
