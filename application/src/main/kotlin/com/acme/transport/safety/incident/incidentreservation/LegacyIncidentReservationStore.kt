package com.acme.transport.safety.incident.incidentreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps legacy incident reservations by code. */
interface LegacyIncidentReservationStore {
    fun find(code: String): LegacyIncidentReservation?
    fun save(item: LegacyIncidentReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyIncidentReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyIncidentReservationStore(
    var items: MutableMap<String, LegacyIncidentReservation>,
    var order: MutableList<String>,
) : LegacyIncidentReservationStore {
    override fun find(code: String): LegacyIncidentReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyIncidentReservation) {
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

    override fun list(): MutableList<LegacyIncidentReservation> {
        val out = mutableListOf<LegacyIncidentReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyIncidentReservationStore(): LegacyIncidentReservationStore {
    return MemoryLegacyIncidentReservationStore(
        mutableMapOf<String, LegacyIncidentReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyIncidentReservationStore(
    var inner: LegacyIncidentReservationStore,
    var cache: TtlCache<LegacyIncidentReservation>,
    var hits: Int,
    var misses: Int,
) : LegacyIncidentReservationStore {
    override fun find(code: String): LegacyIncidentReservation? {
        val hit = (cache.get(code) as LegacyIncidentReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyIncidentReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyIncidentReservation> = inner.list()
}

fun newCachedLegacyIncidentReservationStore(inner: LegacyIncidentReservationStore, ttl: Int, clock: Clock): LegacyIncidentReservationStore {
    return CachedLegacyIncidentReservationStore(
        inner = inner,
        cache = newTtlCache<LegacyIncidentReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy incident reservations. Empty fields match all. */
data class LegacyIncidentReservationQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyIncidentReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyIncidentReservation>): MutableList<LegacyIncidentReservation> {
        val matched = filter(items) { candidate: LegacyIncidentReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyIncidentReservations(store: LegacyIncidentReservationStore, query: LegacyIncidentReservationQuery): MutableList<LegacyIncidentReservation> {
    return query.apply(store.list())
}
