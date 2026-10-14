package com.acme.transport.safety.incident.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident reservations by code. */
interface IncidentReservationStore {
    fun find(code: String): IncidentReservation?
    fun save(item: IncidentReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentReservationStore(
    var items: MutableMap<String, IncidentReservation>,
    var order: MutableList<String>,
) : IncidentReservationStore {
    override fun find(code: String): IncidentReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentReservation) {
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

    override fun list(): MutableList<IncidentReservation> {
        val out = mutableListOf<IncidentReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentReservationStore(): IncidentReservationStore {
    return MemoryIncidentReservationStore(mutableMapOf<String, IncidentReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentReservationStore(
    var inner: IncidentReservationStore,
    var cache: TtlCache<IncidentReservation>,
    var hits: Int,
    var misses: Int,
) : IncidentReservationStore {
    override fun find(code: String): IncidentReservation? {
        val hit = (cache.get(code) as IncidentReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentReservation> = inner.list()
}

fun newCachedIncidentReservationStore(inner: IncidentReservationStore, ttl: Int, clock: Clock): IncidentReservationStore {
    return CachedIncidentReservationStore(
        inner = inner,
        cache = newTtlCache<IncidentReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident reservations. Empty fields match all. */
data class IncidentReservationQuery(
    var category: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentReservation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentReservation>): MutableList<IncidentReservation> {
        val matched = filter(items) { candidate: IncidentReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentReservations(store: IncidentReservationStore, query: IncidentReservationQuery): MutableList<IncidentReservation> {
    return query.apply(store.list())
}
