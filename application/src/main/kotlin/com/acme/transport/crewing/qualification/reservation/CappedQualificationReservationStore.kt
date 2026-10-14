package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped qualification reservations by code. */
interface CappedQualificationReservationStore {
    fun find(code: String): CappedQualificationReservation?
    fun save(item: CappedQualificationReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedQualificationReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedQualificationReservationStore(
    var items: MutableMap<String, CappedQualificationReservation>,
    var order: MutableList<String>,
) : CappedQualificationReservationStore {
    override fun find(code: String): CappedQualificationReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedQualificationReservation) {
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

    override fun list(): MutableList<CappedQualificationReservation> {
        val out = mutableListOf<CappedQualificationReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedQualificationReservationStore(): CappedQualificationReservationStore {
    return MemoryCappedQualificationReservationStore(
        mutableMapOf<String, CappedQualificationReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedQualificationReservationStore(
    var inner: CappedQualificationReservationStore,
    var cache: TtlCache<CappedQualificationReservation>,
    var hits: Int,
    var misses: Int,
) : CappedQualificationReservationStore {
    override fun find(code: String): CappedQualificationReservation? {
        val hit = (cache.get(code) as CappedQualificationReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedQualificationReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedQualificationReservation> = inner.list()
}

fun newCachedCappedQualificationReservationStore(inner: CappedQualificationReservationStore, ttl: Int, clock: Clock): CappedQualificationReservationStore {
    return CachedCappedQualificationReservationStore(
        inner = inner,
        cache = newTtlCache<CappedQualificationReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped qualification reservations. Empty fields match all. */
data class CappedQualificationReservationQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedQualificationReservation): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<CappedQualificationReservation>): MutableList<CappedQualificationReservation> {
        val matched = filter(items) { candidate: CappedQualificationReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedQualificationReservations(store: CappedQualificationReservationStore, query: CappedQualificationReservationQuery): MutableList<CappedQualificationReservation> {
    return query.apply(store.list())
}
