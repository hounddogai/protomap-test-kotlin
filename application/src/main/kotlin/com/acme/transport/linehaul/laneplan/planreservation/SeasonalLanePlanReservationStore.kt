package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal lane plan reservations by code. */
interface SeasonalLanePlanReservationStore {
    fun find(code: String): SeasonalLanePlanReservation?
    fun save(item: SeasonalLanePlanReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalLanePlanReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalLanePlanReservationStore(
    var items: MutableMap<String, SeasonalLanePlanReservation>,
    var order: MutableList<String>,
) : SeasonalLanePlanReservationStore {
    override fun find(code: String): SeasonalLanePlanReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalLanePlanReservation) {
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

    override fun list(): MutableList<SeasonalLanePlanReservation> {
        val out = mutableListOf<SeasonalLanePlanReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalLanePlanReservationStore(): SeasonalLanePlanReservationStore {
    return MemorySeasonalLanePlanReservationStore(
        mutableMapOf<String, SeasonalLanePlanReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalLanePlanReservationStore(
    var inner: SeasonalLanePlanReservationStore,
    var cache: TtlCache<SeasonalLanePlanReservation>,
    var hits: Int,
    var misses: Int,
) : SeasonalLanePlanReservationStore {
    override fun find(code: String): SeasonalLanePlanReservation? {
        val hit = (cache.get(code) as SeasonalLanePlanReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalLanePlanReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalLanePlanReservation> = inner.list()
}

fun newCachedSeasonalLanePlanReservationStore(inner: SeasonalLanePlanReservationStore, ttl: Int, clock: Clock): SeasonalLanePlanReservationStore {
    return CachedSeasonalLanePlanReservationStore(
        inner = inner,
        cache = newTtlCache<SeasonalLanePlanReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal lane plan reservations. Empty fields match all. */
data class SeasonalLanePlanReservationQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalLanePlanReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<SeasonalLanePlanReservation>): MutableList<SeasonalLanePlanReservation> {
        val matched = filter(items) { candidate: SeasonalLanePlanReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalLanePlanReservations(store: SeasonalLanePlanReservationStore, query: SeasonalLanePlanReservationQuery): MutableList<SeasonalLanePlanReservation> {
    return query.apply(store.list())
}
