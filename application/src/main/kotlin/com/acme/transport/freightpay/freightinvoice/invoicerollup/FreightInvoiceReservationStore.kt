package com.acme.transport.freightpay.freightinvoice.invoicerollup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps freight invoice reservations by code. */
interface FreightInvoiceReservationStore {
    fun find(code: String): FreightInvoiceReservation?
    fun save(item: FreightInvoiceReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<FreightInvoiceReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFreightInvoiceReservationStore(
    var items: MutableMap<String, FreightInvoiceReservation>,
    var order: MutableList<String>,
) : FreightInvoiceReservationStore {
    override fun find(code: String): FreightInvoiceReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FreightInvoiceReservation) {
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

    override fun list(): MutableList<FreightInvoiceReservation> {
        val out = mutableListOf<FreightInvoiceReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFreightInvoiceReservationStore(): FreightInvoiceReservationStore {
    return MemoryFreightInvoiceReservationStore(
        mutableMapOf<String, FreightInvoiceReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFreightInvoiceReservationStore(
    var inner: FreightInvoiceReservationStore,
    var cache: TtlCache<FreightInvoiceReservation>,
    var hits: Int,
    var misses: Int,
) : FreightInvoiceReservationStore {
    override fun find(code: String): FreightInvoiceReservation? {
        val hit = (cache.get(code) as FreightInvoiceReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FreightInvoiceReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FreightInvoiceReservation> = inner.list()
}

fun newCachedFreightInvoiceReservationStore(inner: FreightInvoiceReservationStore, ttl: Int, clock: Clock): FreightInvoiceReservationStore {
    return CachedFreightInvoiceReservationStore(
        inner = inner,
        cache = newTtlCache<FreightInvoiceReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects freight invoice reservations. Empty fields match all. */
data class FreightInvoiceReservationQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FreightInvoiceReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FreightInvoiceReservation>): MutableList<FreightInvoiceReservation> {
        val matched = filter(items) { candidate: FreightInvoiceReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFreightInvoiceReservations(store: FreightInvoiceReservationStore, query: FreightInvoiceReservationQuery): MutableList<FreightInvoiceReservation> {
    return query.apply(store.list())
}
