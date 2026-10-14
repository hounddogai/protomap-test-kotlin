package com.acme.transport.terminal.appointment.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps appointment reservations by code. */
interface AppointmentReservationStore {
    fun find(code: String): AppointmentReservation?
    fun save(item: AppointmentReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<AppointmentReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAppointmentReservationStore(
    var items: MutableMap<String, AppointmentReservation>,
    var order: MutableList<String>,
) : AppointmentReservationStore {
    override fun find(code: String): AppointmentReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AppointmentReservation) {
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

    override fun list(): MutableList<AppointmentReservation> {
        val out = mutableListOf<AppointmentReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAppointmentReservationStore(): AppointmentReservationStore {
    return MemoryAppointmentReservationStore(
        mutableMapOf<String, AppointmentReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAppointmentReservationStore(
    var inner: AppointmentReservationStore,
    var cache: TtlCache<AppointmentReservation>,
    var hits: Int,
    var misses: Int,
) : AppointmentReservationStore {
    override fun find(code: String): AppointmentReservation? {
        val hit = (cache.get(code) as AppointmentReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AppointmentReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AppointmentReservation> = inner.list()
}

fun newCachedAppointmentReservationStore(inner: AppointmentReservationStore, ttl: Int, clock: Clock): AppointmentReservationStore {
    return CachedAppointmentReservationStore(
        inner = inner,
        cache = newTtlCache<AppointmentReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects appointment reservations. Empty fields match all. */
data class AppointmentReservationQuery(
    var title: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AppointmentReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AppointmentReservation>): MutableList<AppointmentReservation> {
        val matched = filter(items) { candidate: AppointmentReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAppointmentReservations(store: AppointmentReservationStore, query: AppointmentReservationQuery): MutableList<AppointmentReservation> {
    return query.apply(store.list())
}
