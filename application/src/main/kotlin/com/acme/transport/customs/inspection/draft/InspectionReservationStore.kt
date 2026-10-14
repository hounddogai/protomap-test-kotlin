package com.acme.transport.customs.inspection.draft

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection reservations by code. */
interface InspectionReservationStore {
    fun find(code: String): InspectionReservation?
    fun save(item: InspectionReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionReservationStore(
    var items: MutableMap<String, InspectionReservation>,
    var order: MutableList<String>,
) : InspectionReservationStore {
    override fun find(code: String): InspectionReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionReservation) {
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

    override fun list(): MutableList<InspectionReservation> {
        val out = mutableListOf<InspectionReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionReservationStore(): InspectionReservationStore {
    return MemoryInspectionReservationStore(
        mutableMapOf<String, InspectionReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionReservationStore(
    var inner: InspectionReservationStore,
    var cache: TtlCache<InspectionReservation>,
    var hits: Int,
    var misses: Int,
) : InspectionReservationStore {
    override fun find(code: String): InspectionReservation? {
        val hit = (cache.get(code) as InspectionReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionReservation> = inner.list()
}

fun newCachedInspectionReservationStore(inner: InspectionReservationStore, ttl: Int, clock: Clock): InspectionReservationStore {
    return CachedInspectionReservationStore(
        inner = inner,
        cache = newTtlCache<InspectionReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection reservations. Empty fields match all. */
data class InspectionReservationQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionReservation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionReservation>): MutableList<InspectionReservation> {
        val matched = filter(items) { candidate: InspectionReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionReservations(store: InspectionReservationStore, query: InspectionReservationQuery): MutableList<InspectionReservation> {
    return query.apply(store.list())
}
