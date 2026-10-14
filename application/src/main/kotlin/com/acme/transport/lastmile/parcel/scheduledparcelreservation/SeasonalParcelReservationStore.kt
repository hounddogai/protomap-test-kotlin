package com.acme.transport.lastmile.parcel.scheduledparcelreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps seasonal parcel reservations by code. */
interface SeasonalParcelReservationStore {
    fun find(code: String): SeasonalParcelReservation?
    fun save(item: SeasonalParcelReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalParcelReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalParcelReservationStore(
    var items: MutableMap<String, SeasonalParcelReservation>,
    var order: MutableList<String>,
) : SeasonalParcelReservationStore {
    override fun find(code: String): SeasonalParcelReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalParcelReservation) {
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

    override fun list(): MutableList<SeasonalParcelReservation> {
        val out = mutableListOf<SeasonalParcelReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalParcelReservationStore(): SeasonalParcelReservationStore {
    return MemorySeasonalParcelReservationStore(
        mutableMapOf<String, SeasonalParcelReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalParcelReservationStore(
    var inner: SeasonalParcelReservationStore,
    var cache: TtlCache<SeasonalParcelReservation>,
    var hits: Int,
    var misses: Int,
) : SeasonalParcelReservationStore {
    override fun find(code: String): SeasonalParcelReservation? {
        val hit = (cache.get(code) as SeasonalParcelReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalParcelReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalParcelReservation> = inner.list()
}

fun newCachedSeasonalParcelReservationStore(inner: SeasonalParcelReservationStore, ttl: Int, clock: Clock): SeasonalParcelReservationStore {
    return CachedSeasonalParcelReservationStore(
        inner = inner,
        cache = newTtlCache<SeasonalParcelReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal parcel reservations. Empty fields match all. */
data class SeasonalParcelReservationQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalParcelReservation): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalParcelReservation>): MutableList<SeasonalParcelReservation> {
        val matched = filter(items) { candidate: SeasonalParcelReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalParcelReservations(store: SeasonalParcelReservationStore, query: SeasonalParcelReservationQuery): MutableList<SeasonalParcelReservation> {
    return query.apply(store.list())
}
