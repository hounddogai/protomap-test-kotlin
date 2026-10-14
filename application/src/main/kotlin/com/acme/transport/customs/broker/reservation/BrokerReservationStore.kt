package com.acme.transport.customs.broker.reservation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker reservations by code. */
interface BrokerReservationStore {
    fun find(code: String): BrokerReservation?
    fun save(item: BrokerReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerReservationStore(
    var items: MutableMap<String, BrokerReservation>,
    var order: MutableList<String>,
) : BrokerReservationStore {
    override fun find(code: String): BrokerReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerReservation) {
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

    override fun list(): MutableList<BrokerReservation> {
        val out = mutableListOf<BrokerReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerReservationStore(): BrokerReservationStore {
    return MemoryBrokerReservationStore(mutableMapOf<String, BrokerReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerReservationStore(
    var inner: BrokerReservationStore,
    var cache: TtlCache<BrokerReservation>,
    var hits: Int,
    var misses: Int,
) : BrokerReservationStore {
    override fun find(code: String): BrokerReservation? {
        val hit = (cache.get(code) as BrokerReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerReservation> = inner.list()
}

fun newCachedBrokerReservationStore(inner: BrokerReservationStore, ttl: Int, clock: Clock): BrokerReservationStore {
    return CachedBrokerReservationStore(
        inner = inner,
        cache = newTtlCache<BrokerReservation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker reservations. Empty fields match all. */
data class BrokerReservationQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<BrokerReservation>): MutableList<BrokerReservation> {
        val matched = filter(items) { candidate: BrokerReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerReservations(store: BrokerReservationStore, query: BrokerReservationQuery): MutableList<BrokerReservation> {
    return query.apply(store.list())
}
