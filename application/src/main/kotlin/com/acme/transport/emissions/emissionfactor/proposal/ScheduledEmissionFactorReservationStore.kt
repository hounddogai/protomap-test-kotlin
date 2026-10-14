package com.acme.transport.emissions.emissionfactor.proposal

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled emission factor reservations by code. */
interface ScheduledEmissionFactorReservationStore {
    fun find(code: String): ScheduledEmissionFactorReservation?
    fun save(item: ScheduledEmissionFactorReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledEmissionFactorReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledEmissionFactorReservationStore(
    var items: MutableMap<String, ScheduledEmissionFactorReservation>,
    var order: MutableList<String>,
) : ScheduledEmissionFactorReservationStore {
    override fun find(code: String): ScheduledEmissionFactorReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledEmissionFactorReservation) {
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

    override fun list(): MutableList<ScheduledEmissionFactorReservation> {
        val out = mutableListOf<ScheduledEmissionFactorReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledEmissionFactorReservationStore(): ScheduledEmissionFactorReservationStore {
    return MemoryScheduledEmissionFactorReservationStore(
        mutableMapOf<String, ScheduledEmissionFactorReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledEmissionFactorReservationStore(
    var inner: ScheduledEmissionFactorReservationStore,
    var cache: TtlCache<ScheduledEmissionFactorReservation>,
    var hits: Int,
    var misses: Int,
) : ScheduledEmissionFactorReservationStore {
    override fun find(code: String): ScheduledEmissionFactorReservation? {
        val hit = (cache.get(code) as ScheduledEmissionFactorReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledEmissionFactorReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledEmissionFactorReservation> = inner.list()
}

fun newCachedScheduledEmissionFactorReservationStore(inner: ScheduledEmissionFactorReservationStore, ttl: Int, clock: Clock): ScheduledEmissionFactorReservationStore {
    return CachedScheduledEmissionFactorReservationStore(
        inner = inner,
        cache = newTtlCache<ScheduledEmissionFactorReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled emission factor reservations. Empty fields match all. */
data class ScheduledEmissionFactorReservationQuery(
    var locale: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledEmissionFactorReservation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledEmissionFactorReservation>): MutableList<ScheduledEmissionFactorReservation> {
        val matched = filter(items) { candidate: ScheduledEmissionFactorReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledEmissionFactorReservations(store: ScheduledEmissionFactorReservationStore, query: ScheduledEmissionFactorReservationQuery): MutableList<ScheduledEmissionFactorReservation> {
    return query.apply(store.list())
}
