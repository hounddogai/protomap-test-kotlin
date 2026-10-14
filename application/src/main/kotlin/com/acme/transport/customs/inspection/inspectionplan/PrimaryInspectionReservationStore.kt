package com.acme.transport.customs.inspection.inspectionplan

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary inspection reservations by code. */
interface PrimaryInspectionReservationStore {
    fun find(code: String): PrimaryInspectionReservation?
    fun save(item: PrimaryInspectionReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryInspectionReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryInspectionReservationStore(
    var items: MutableMap<String, PrimaryInspectionReservation>,
    var order: MutableList<String>,
) : PrimaryInspectionReservationStore {
    override fun find(code: String): PrimaryInspectionReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryInspectionReservation) {
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

    override fun list(): MutableList<PrimaryInspectionReservation> {
        val out = mutableListOf<PrimaryInspectionReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryInspectionReservationStore(): PrimaryInspectionReservationStore {
    return MemoryPrimaryInspectionReservationStore(
        mutableMapOf<String, PrimaryInspectionReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryInspectionReservationStore(
    var inner: PrimaryInspectionReservationStore,
    var cache: TtlCache<PrimaryInspectionReservation>,
    var hits: Int,
    var misses: Int,
) : PrimaryInspectionReservationStore {
    override fun find(code: String): PrimaryInspectionReservation? {
        val hit = (cache.get(code) as PrimaryInspectionReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryInspectionReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryInspectionReservation> = inner.list()
}

fun newCachedPrimaryInspectionReservationStore(inner: PrimaryInspectionReservationStore, ttl: Int, clock: Clock): PrimaryInspectionReservationStore {
    return CachedPrimaryInspectionReservationStore(
        inner = inner,
        cache = newTtlCache<PrimaryInspectionReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary inspection reservations. Empty fields match all. */
data class PrimaryInspectionReservationQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryInspectionReservation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryInspectionReservation>): MutableList<PrimaryInspectionReservation> {
        val matched = filter(items) { candidate: PrimaryInspectionReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryInspectionReservations(store: PrimaryInspectionReservationStore, query: PrimaryInspectionReservationQuery): MutableList<PrimaryInspectionReservation> {
    return query.apply(store.list())
}
