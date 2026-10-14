package com.acme.transport.freightpay.freightinvoice.invoicesummary

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive freight invoice reservations by code. */
interface AdaptiveFreightInvoiceReservationStore {
    fun find(code: String): AdaptiveFreightInvoiceReservation?
    fun save(item: AdaptiveFreightInvoiceReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveFreightInvoiceReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveFreightInvoiceReservationStore(
    var items: MutableMap<String, AdaptiveFreightInvoiceReservation>,
    var order: MutableList<String>,
) : AdaptiveFreightInvoiceReservationStore {
    override fun find(code: String): AdaptiveFreightInvoiceReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveFreightInvoiceReservation) {
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

    override fun list(): MutableList<AdaptiveFreightInvoiceReservation> {
        val out = mutableListOf<AdaptiveFreightInvoiceReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveFreightInvoiceReservationStore(): AdaptiveFreightInvoiceReservationStore {
    return MemoryAdaptiveFreightInvoiceReservationStore(
        mutableMapOf<String, AdaptiveFreightInvoiceReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveFreightInvoiceReservationStore(
    var inner: AdaptiveFreightInvoiceReservationStore,
    var cache: TtlCache<AdaptiveFreightInvoiceReservation>,
    var hits: Int,
    var misses: Int,
) : AdaptiveFreightInvoiceReservationStore {
    override fun find(code: String): AdaptiveFreightInvoiceReservation? {
        val hit = (cache.get(code) as AdaptiveFreightInvoiceReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveFreightInvoiceReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveFreightInvoiceReservation> = inner.list()
}

fun newCachedAdaptiveFreightInvoiceReservationStore(inner: AdaptiveFreightInvoiceReservationStore, ttl: Int, clock: Clock): AdaptiveFreightInvoiceReservationStore {
    return CachedAdaptiveFreightInvoiceReservationStore(
        inner = inner,
        cache = newTtlCache<AdaptiveFreightInvoiceReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive freight invoice reservations. Empty fields match all. */
data class AdaptiveFreightInvoiceReservationQuery(
    var unit: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveFreightInvoiceReservation): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveFreightInvoiceReservation>): MutableList<AdaptiveFreightInvoiceReservation> {
        val matched = filter(items) { candidate: AdaptiveFreightInvoiceReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveFreightInvoiceReservations(store: AdaptiveFreightInvoiceReservationStore, query: AdaptiveFreightInvoiceReservationQuery): MutableList<AdaptiveFreightInvoiceReservation> {
    return query.apply(store.list())
}
