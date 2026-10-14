package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity reservations by code. */
interface CapacityReservationStore {
    fun find(code: String): CapacityReservation?
    fun save(item: CapacityReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityReservationStore(
    var items: MutableMap<String, CapacityReservation>,
    var order: MutableList<String>,
) : CapacityReservationStore {
    override fun find(code: String): CapacityReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityReservation) {
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

    override fun list(): MutableList<CapacityReservation> {
        val out = mutableListOf<CapacityReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityReservationStore(): CapacityReservationStore {
    return MemoryCapacityReservationStore(mutableMapOf<String, CapacityReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityReservationStore(
    var inner: CapacityReservationStore,
    var cache: TtlCache<CapacityReservation>,
    var hits: Int,
    var misses: Int,
) : CapacityReservationStore {
    override fun find(code: String): CapacityReservation? {
        val hit = (cache.get(code) as CapacityReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityReservation> = inner.list()
}

fun newCachedCapacityReservationStore(inner: CapacityReservationStore, ttl: Int, clock: Clock): CapacityReservationStore {
    return CachedCapacityReservationStore(
        inner = inner,
        cache = newTtlCache<CapacityReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity reservations. Empty fields match all. */
data class CapacityReservationQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CapacityReservation>): MutableList<CapacityReservation> {
        val matched = filter(items) { candidate: CapacityReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityReservations(store: CapacityReservationStore, query: CapacityReservationQuery): MutableList<CapacityReservation> {
    return query.apply(store.list())
}
