package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender reservations by code. */
interface TenderReservationStore {
    fun find(code: String): TenderReservation?
    fun save(item: TenderReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderReservationStore(
    var items: MutableMap<String, TenderReservation>,
    var order: MutableList<String>,
) : TenderReservationStore {
    override fun find(code: String): TenderReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderReservation) {
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

    override fun list(): MutableList<TenderReservation> {
        val out = mutableListOf<TenderReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderReservationStore(): TenderReservationStore {
    return MemoryTenderReservationStore(mutableMapOf<String, TenderReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderReservationStore(
    var inner: TenderReservationStore,
    var cache: TtlCache<TenderReservation>,
    var hits: Int,
    var misses: Int,
) : TenderReservationStore {
    override fun find(code: String): TenderReservation? {
        val hit = (cache.get(code) as TenderReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderReservation> = inner.list()
}

fun newCachedTenderReservationStore(inner: TenderReservationStore, ttl: Int, clock: Clock): TenderReservationStore {
    return CachedTenderReservationStore(
        inner = inner,
        cache = newTtlCache<TenderReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender reservations. Empty fields match all. */
data class TenderReservationQuery(
    var region: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderReservation): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<TenderReservation>): MutableList<TenderReservation> {
        val matched = filter(items) { candidate: TenderReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderReservations(store: TenderReservationStore, query: TenderReservationQuery): MutableList<TenderReservation> {
    return query.apply(store.list())
}
