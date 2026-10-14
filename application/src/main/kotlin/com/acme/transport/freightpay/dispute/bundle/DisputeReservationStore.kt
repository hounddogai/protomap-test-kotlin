package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute reservations by code. */
interface DisputeReservationStore {
    fun find(code: String): DisputeReservation?
    fun save(item: DisputeReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeReservationStore(
    var items: MutableMap<String, DisputeReservation>,
    var order: MutableList<String>,
) : DisputeReservationStore {
    override fun find(code: String): DisputeReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeReservation) {
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

    override fun list(): MutableList<DisputeReservation> {
        val out = mutableListOf<DisputeReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeReservationStore(): DisputeReservationStore {
    return MemoryDisputeReservationStore(mutableMapOf<String, DisputeReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeReservationStore(
    var inner: DisputeReservationStore,
    var cache: TtlCache<DisputeReservation>,
    var hits: Int,
    var misses: Int,
) : DisputeReservationStore {
    override fun find(code: String): DisputeReservation? {
        val hit = (cache.get(code) as DisputeReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeReservation> = inner.list()
}

fun newCachedDisputeReservationStore(inner: DisputeReservationStore, ttl: Int, clock: Clock): DisputeReservationStore {
    return CachedDisputeReservationStore(
        inner = inner,
        cache = newTtlCache<DisputeReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute reservations. Empty fields match all. */
data class DisputeReservationQuery(
    var channel: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeReservation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeReservation>): MutableList<DisputeReservation> {
        val matched = filter(items) { candidate: DisputeReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeReservations(store: DisputeReservationStore, query: DisputeReservationQuery): MutableList<DisputeReservation> {
    return query.apply(store.list())
}
