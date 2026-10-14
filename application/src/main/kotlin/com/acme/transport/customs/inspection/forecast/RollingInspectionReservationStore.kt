package com.acme.transport.customs.inspection.forecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling inspection reservations by code. */
interface RollingInspectionReservationStore {
    fun find(code: String): RollingInspectionReservation?
    fun save(item: RollingInspectionReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingInspectionReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingInspectionReservationStore(
    var items: MutableMap<String, RollingInspectionReservation>,
    var order: MutableList<String>,
) : RollingInspectionReservationStore {
    override fun find(code: String): RollingInspectionReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingInspectionReservation) {
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

    override fun list(): MutableList<RollingInspectionReservation> {
        val out = mutableListOf<RollingInspectionReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingInspectionReservationStore(): RollingInspectionReservationStore {
    return MemoryRollingInspectionReservationStore(
        mutableMapOf<String, RollingInspectionReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingInspectionReservationStore(
    var inner: RollingInspectionReservationStore,
    var cache: TtlCache<RollingInspectionReservation>,
    var hits: Int,
    var misses: Int,
) : RollingInspectionReservationStore {
    override fun find(code: String): RollingInspectionReservation? {
        val hit = (cache.get(code) as RollingInspectionReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingInspectionReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingInspectionReservation> = inner.list()
}

fun newCachedRollingInspectionReservationStore(inner: RollingInspectionReservationStore, ttl: Int, clock: Clock): RollingInspectionReservationStore {
    return CachedRollingInspectionReservationStore(
        inner = inner,
        cache = newTtlCache<RollingInspectionReservation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling inspection reservations. Empty fields match all. */
data class RollingInspectionReservationQuery(
    var category: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingInspectionReservation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingInspectionReservation>): MutableList<RollingInspectionReservation> {
        val matched = filter(items) { candidate: RollingInspectionReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingInspectionReservations(store: RollingInspectionReservationStore, query: RollingInspectionReservationQuery): MutableList<RollingInspectionReservation> {
    return query.apply(store.list())
}
