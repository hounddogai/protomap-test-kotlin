package com.acme.transport.fleetops.tire.preference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling tire reservations by code. */
interface RollingTireReservationStore {
    fun find(code: String): RollingTireReservation?
    fun save(item: RollingTireReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTireReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTireReservationStore(
    var items: MutableMap<String, RollingTireReservation>,
    var order: MutableList<String>,
) : RollingTireReservationStore {
    override fun find(code: String): RollingTireReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTireReservation) {
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

    override fun list(): MutableList<RollingTireReservation> {
        val out = mutableListOf<RollingTireReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTireReservationStore(): RollingTireReservationStore {
    return MemoryRollingTireReservationStore(
        mutableMapOf<String, RollingTireReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTireReservationStore(
    var inner: RollingTireReservationStore,
    var cache: TtlCache<RollingTireReservation>,
    var hits: Int,
    var misses: Int,
) : RollingTireReservationStore {
    override fun find(code: String): RollingTireReservation? {
        val hit = (cache.get(code) as RollingTireReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTireReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTireReservation> = inner.list()
}

fun newCachedRollingTireReservationStore(inner: RollingTireReservationStore, ttl: Int, clock: Clock): RollingTireReservationStore {
    return CachedRollingTireReservationStore(
        inner = inner,
        cache = newTtlCache<RollingTireReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling tire reservations. Empty fields match all. */
data class RollingTireReservationQuery(
    var unit: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTireReservation): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingTireReservation>): MutableList<RollingTireReservation> {
        val matched = filter(items) { candidate: RollingTireReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTireReservations(store: RollingTireReservationStore, query: RollingTireReservationQuery): MutableList<RollingTireReservation> {
    return query.apply(store.list())
}
