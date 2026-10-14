package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal trailer yard reservations by code. */
interface SeasonalTrailerYardReservationStore {
    fun find(code: String): SeasonalTrailerYardReservation?
    fun save(item: SeasonalTrailerYardReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTrailerYardReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTrailerYardReservationStore(
    var items: MutableMap<String, SeasonalTrailerYardReservation>,
    var order: MutableList<String>,
) : SeasonalTrailerYardReservationStore {
    override fun find(code: String): SeasonalTrailerYardReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTrailerYardReservation) {
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

    override fun list(): MutableList<SeasonalTrailerYardReservation> {
        val out = mutableListOf<SeasonalTrailerYardReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTrailerYardReservationStore(): SeasonalTrailerYardReservationStore {
    return MemorySeasonalTrailerYardReservationStore(
        mutableMapOf<String, SeasonalTrailerYardReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTrailerYardReservationStore(
    var inner: SeasonalTrailerYardReservationStore,
    var cache: TtlCache<SeasonalTrailerYardReservation>,
    var hits: Int,
    var misses: Int,
) : SeasonalTrailerYardReservationStore {
    override fun find(code: String): SeasonalTrailerYardReservation? {
        val hit = (cache.get(code) as SeasonalTrailerYardReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTrailerYardReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTrailerYardReservation> = inner.list()
}

fun newCachedSeasonalTrailerYardReservationStore(inner: SeasonalTrailerYardReservationStore, ttl: Int, clock: Clock): SeasonalTrailerYardReservationStore {
    return CachedSeasonalTrailerYardReservationStore(
        inner = inner,
        cache = newTtlCache<SeasonalTrailerYardReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal trailer yard reservations. Empty fields match all. */
data class SeasonalTrailerYardReservationQuery(
    var depot: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTrailerYardReservation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTrailerYardReservation>): MutableList<SeasonalTrailerYardReservation> {
        val matched = filter(items) { candidate: SeasonalTrailerYardReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTrailerYardReservations(store: SeasonalTrailerYardReservationStore, query: SeasonalTrailerYardReservationQuery): MutableList<SeasonalTrailerYardReservation> {
    return query.apply(store.list())
}
