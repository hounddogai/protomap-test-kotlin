package com.acme.transport.freightpay.ratecard.reservation

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary rate card reservations by code. */
interface PrimaryRateCardReservationStore {
    fun find(code: String): PrimaryRateCardReservation?
    fun save(item: PrimaryRateCardReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRateCardReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRateCardReservationStore(
    var items: MutableMap<String, PrimaryRateCardReservation>,
    var order: MutableList<String>,
) : PrimaryRateCardReservationStore {
    override fun find(code: String): PrimaryRateCardReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRateCardReservation) {
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

    override fun list(): MutableList<PrimaryRateCardReservation> {
        val out = mutableListOf<PrimaryRateCardReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRateCardReservationStore(): PrimaryRateCardReservationStore {
    return MemoryPrimaryRateCardReservationStore(
        mutableMapOf<String, PrimaryRateCardReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRateCardReservationStore(
    var inner: PrimaryRateCardReservationStore,
    var cache: TtlCache<PrimaryRateCardReservation>,
    var hits: Int,
    var misses: Int,
) : PrimaryRateCardReservationStore {
    override fun find(code: String): PrimaryRateCardReservation? {
        val hit = (cache.get(code) as PrimaryRateCardReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRateCardReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRateCardReservation> = inner.list()
}

fun newCachedPrimaryRateCardReservationStore(inner: PrimaryRateCardReservationStore, ttl: Int, clock: Clock): PrimaryRateCardReservationStore {
    return CachedPrimaryRateCardReservationStore(
        inner = inner,
        cache = newTtlCache<PrimaryRateCardReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary rate card reservations. Empty fields match all. */
data class PrimaryRateCardReservationQuery(
    var locale: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRateCardReservation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryRateCardReservation>): MutableList<PrimaryRateCardReservation> {
        val matched = filter(items) { candidate: PrimaryRateCardReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRateCardReservations(store: PrimaryRateCardReservationStore, query: PrimaryRateCardReservationQuery): MutableList<PrimaryRateCardReservation> {
    return query.apply(store.list())
}
