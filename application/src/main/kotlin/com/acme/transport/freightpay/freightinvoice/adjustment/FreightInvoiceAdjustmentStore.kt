package com.acme.transport.freightpay.freightinvoice.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice adjustments by code. */
interface FreightInvoiceAdjustmentStore {
    fun find(code: String): FreightInvoiceAdjustment?
    fun save(item: FreightInvoiceAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceAdjustmentStore(
    var items: MutableMap<String, FreightInvoiceAdjustment>,
    var order: MutableList<String>,
) : FreightInvoiceAdjustmentStore {
    override fun find(code: String): FreightInvoiceAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceAdjustment) {
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

    override fun list(): MutableList<FreightInvoiceAdjustment> {
        val out = mutableListOf<FreightInvoiceAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceAdjustmentStore(): FreightInvoiceAdjustmentStore {
    return MemoryFreightInvoiceAdjustmentStore(
        mutableMapOf<String, FreightInvoiceAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceAdjustmentStore(
    var inner: FreightInvoiceAdjustmentStore,
    var cache: TtlCache<FreightInvoiceAdjustment>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceAdjustmentStore {
    override fun find(code: String): FreightInvoiceAdjustment? {
        val hit = (cache.get(code) as FreightInvoiceAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceAdjustment> = inner.list()
}

fun newCachedFreightInvoiceAdjustmentStore(inner: FreightInvoiceAdjustmentStore, ttl: Int, clock: Clock): FreightInvoiceAdjustmentStore {
    return CachedFreightInvoiceAdjustmentStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceAdjustment>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice adjustments. Empty fields match all. */
data class FreightInvoiceAdjustmentQuery(
    var category: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceAdjustment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceAdjustment>): MutableList<FreightInvoiceAdjustment> {
        val matched = filter(items) { candidate: FreightInvoiceAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceAdjustments(store: FreightInvoiceAdjustmentStore, query: FreightInvoiceAdjustmentQuery): MutableList<FreightInvoiceAdjustment> {
    return query.apply(store.list())
}
