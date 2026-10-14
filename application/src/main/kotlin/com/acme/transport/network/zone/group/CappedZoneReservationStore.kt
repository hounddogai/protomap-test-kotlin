package com.acme.transport.network.zone.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped zone reservations by code. */
interface CappedZoneReservationStore {
    fun find(code: String): CappedZoneReservation?
    fun save(item: CappedZoneReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedZoneReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedZoneReservationStore(
    var items: MutableMap<String, CappedZoneReservation>,
    var order: MutableList<String>,
) : CappedZoneReservationStore {
    override fun find(code: String): CappedZoneReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedZoneReservation) {
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

    override fun list(): MutableList<CappedZoneReservation> {
        val out = mutableListOf<CappedZoneReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedZoneReservationStore(): CappedZoneReservationStore {
    return MemoryCappedZoneReservationStore(
        mutableMapOf<String, CappedZoneReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedZoneReservationStore(
    var inner: CappedZoneReservationStore,
    var cache: TtlCache<CappedZoneReservation>,
    var hits: Int,
    var misses: Int,
) : CappedZoneReservationStore {
    override fun find(code: String): CappedZoneReservation? {
        val hit = (cache.get(code) as CappedZoneReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedZoneReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedZoneReservation> = inner.list()
}

fun newCachedCappedZoneReservationStore(inner: CappedZoneReservationStore, ttl: Int, clock: Clock): CappedZoneReservationStore {
    return CachedCappedZoneReservationStore(
        inner = inner,
        cache = newTtlCache<CappedZoneReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped zone reservations. Empty fields match all. */
data class CappedZoneReservationQuery(
    var locale: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedZoneReservation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedZoneReservation>): MutableList<CappedZoneReservation> {
        val matched = filter(items) { candidate: CappedZoneReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedZoneReservations(store: CappedZoneReservationStore, query: CappedZoneReservationQuery): MutableList<CappedZoneReservation> {
    return query.apply(store.list())
}
