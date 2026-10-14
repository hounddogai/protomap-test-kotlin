package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice plans by code. */
interface FreightInvoicePlanStore {
    fun find(code: String): FreightInvoicePlan?
    fun save(item: FreightInvoicePlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoicePlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoicePlanStore(
    var items: MutableMap<String, FreightInvoicePlan>,
    var order: MutableList<String>,
) : FreightInvoicePlanStore {
    override fun find(code: String): FreightInvoicePlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoicePlan) {
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

    override fun list(): MutableList<FreightInvoicePlan> {
        val out = mutableListOf<FreightInvoicePlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoicePlanStore(): FreightInvoicePlanStore {
    return MemoryFreightInvoicePlanStore(mutableMapOf<String, FreightInvoicePlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoicePlanStore(
    var inner: FreightInvoicePlanStore,
    var cache: TtlCache<FreightInvoicePlan>,
    var hits: Int,
    var misses: Int,
) : FreightInvoicePlanStore {
    override fun find(code: String): FreightInvoicePlan? {
        val hit = (cache.get(code) as FreightInvoicePlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoicePlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoicePlan> = inner.list()
}

fun newCachedFreightInvoicePlanStore(inner: FreightInvoicePlanStore, ttl: Int, clock: Clock): FreightInvoicePlanStore {
    return CachedFreightInvoicePlanStore(
        inner = inner,
        cache = newTtlCache<FreightInvoicePlan>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice plans. Empty fields match all. */
data class FreightInvoicePlanQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoicePlan): Boolean {
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

    fun apply(items: MutableList<FreightInvoicePlan>): MutableList<FreightInvoicePlan> {
        val matched = filter(items) { candidate: FreightInvoicePlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoicePlans(store: FreightInvoicePlanStore, query: FreightInvoicePlanQuery): MutableList<FreightInvoicePlan> {
    return query.apply(store.list())
}
