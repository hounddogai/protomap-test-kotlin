package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict rate card reservations by code. */
interface StrictRateCardReservationStore {
    fun find(code: String): StrictRateCardReservation?
    fun save(item: StrictRateCardReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRateCardReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRateCardReservationStore(
    var items: MutableMap<String, StrictRateCardReservation>,
    var order: MutableList<String>,
) : StrictRateCardReservationStore {
    override fun find(code: String): StrictRateCardReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRateCardReservation) {
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

    override fun list(): MutableList<StrictRateCardReservation> {
        val out = mutableListOf<StrictRateCardReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRateCardReservationStore(): StrictRateCardReservationStore {
    return MemoryStrictRateCardReservationStore(
        mutableMapOf<String, StrictRateCardReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRateCardReservationStore(
    var inner: StrictRateCardReservationStore,
    var cache: TtlCache<StrictRateCardReservation>,
    var hits: Int,
    var misses: Int,
) : StrictRateCardReservationStore {
    override fun find(code: String): StrictRateCardReservation? {
        val hit = (cache.get(code) as StrictRateCardReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRateCardReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRateCardReservation> = inner.list()
}

fun newCachedStrictRateCardReservationStore(inner: StrictRateCardReservationStore, ttl: Int, clock: Clock): StrictRateCardReservationStore {
    return CachedStrictRateCardReservationStore(
        inner = inner,
        cache = newTtlCache<StrictRateCardReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict rate card reservations. Empty fields match all. */
data class StrictRateCardReservationQuery(
    var locale: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRateCardReservation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRateCardReservation>): MutableList<StrictRateCardReservation> {
        val matched = filter(items) { candidate: StrictRateCardReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRateCardReservations(store: StrictRateCardReservationStore, query: StrictRateCardReservationQuery): MutableList<StrictRateCardReservation> {
    return query.apply(store.list())
}
