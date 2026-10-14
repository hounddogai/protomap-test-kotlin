package com.acme.transport.freightpay.dispute.schedule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal dispute reservations by code. */
interface SeasonalDisputeReservationStore {
    fun find(code: String): SeasonalDisputeReservation?
    fun save(item: SeasonalDisputeReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalDisputeReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalDisputeReservationStore(
    var items: MutableMap<String, SeasonalDisputeReservation>,
    var order: MutableList<String>,
) : SeasonalDisputeReservationStore {
    override fun find(code: String): SeasonalDisputeReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalDisputeReservation) {
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

    override fun list(): MutableList<SeasonalDisputeReservation> {
        val out = mutableListOf<SeasonalDisputeReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalDisputeReservationStore(): SeasonalDisputeReservationStore {
    return MemorySeasonalDisputeReservationStore(
        mutableMapOf<String, SeasonalDisputeReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalDisputeReservationStore(
    var inner: SeasonalDisputeReservationStore,
    var cache: TtlCache<SeasonalDisputeReservation>,
    var hits: Int,
    var misses: Int,
) : SeasonalDisputeReservationStore {
    override fun find(code: String): SeasonalDisputeReservation? {
        val hit = (cache.get(code) as SeasonalDisputeReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalDisputeReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalDisputeReservation> = inner.list()
}

fun newCachedSeasonalDisputeReservationStore(inner: SeasonalDisputeReservationStore, ttl: Int, clock: Clock): SeasonalDisputeReservationStore {
    return CachedSeasonalDisputeReservationStore(
        inner = inner,
        cache = newTtlCache<SeasonalDisputeReservation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal dispute reservations. Empty fields match all. */
data class SeasonalDisputeReservationQuery(
    var category: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalDisputeReservation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalDisputeReservation>): MutableList<SeasonalDisputeReservation> {
        val matched = filter(items) { candidate: SeasonalDisputeReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalDisputeReservations(store: SeasonalDisputeReservationStore, query: SeasonalDisputeReservationQuery): MutableList<SeasonalDisputeReservation> {
    return query.apply(store.list())
}
