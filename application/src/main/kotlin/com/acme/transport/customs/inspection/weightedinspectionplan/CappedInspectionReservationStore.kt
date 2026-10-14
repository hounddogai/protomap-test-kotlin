package com.acme.transport.customs.inspection.weightedinspectionplan

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped inspection reservations by code. */
interface CappedInspectionReservationStore {
    fun find(code: String): CappedInspectionReservation?
    fun save(item: CappedInspectionReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedInspectionReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedInspectionReservationStore(
    var items: MutableMap<String, CappedInspectionReservation>,
    var order: MutableList<String>,
) : CappedInspectionReservationStore {
    override fun find(code: String): CappedInspectionReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedInspectionReservation) {
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

    override fun list(): MutableList<CappedInspectionReservation> {
        val out = mutableListOf<CappedInspectionReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedInspectionReservationStore(): CappedInspectionReservationStore {
    return MemoryCappedInspectionReservationStore(
        mutableMapOf<String, CappedInspectionReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedInspectionReservationStore(
    var inner: CappedInspectionReservationStore,
    var cache: TtlCache<CappedInspectionReservation>,
    var hits: Int,
    var misses: Int,
) : CappedInspectionReservationStore {
    override fun find(code: String): CappedInspectionReservation? {
        val hit = (cache.get(code) as CappedInspectionReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedInspectionReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedInspectionReservation> = inner.list()
}

fun newCachedCappedInspectionReservationStore(inner: CappedInspectionReservationStore, ttl: Int, clock: Clock): CappedInspectionReservationStore {
    return CachedCappedInspectionReservationStore(
        inner = inner,
        cache = newTtlCache<CappedInspectionReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped inspection reservations. Empty fields match all. */
data class CappedInspectionReservationQuery(
    var category: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedInspectionReservation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedInspectionReservation>): MutableList<CappedInspectionReservation> {
        val matched = filter(items) { candidate: CappedInspectionReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedInspectionReservations(store: CappedInspectionReservationStore, query: CappedInspectionReservationQuery): MutableList<CappedInspectionReservation> {
    return query.apply(store.list())
}
