package com.acme.transport.safety.violation.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation reservations by code. */
interface ViolationReservationStore {
    fun find(code: String): ViolationReservation?
    fun save(item: ViolationReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationReservationStore(
    var items: MutableMap<String, ViolationReservation>,
    var order: MutableList<String>,
) : ViolationReservationStore {
    override fun find(code: String): ViolationReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationReservation) {
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

    override fun list(): MutableList<ViolationReservation> {
        val out = mutableListOf<ViolationReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationReservationStore(): ViolationReservationStore {
    return MemoryViolationReservationStore(
        mutableMapOf<String, ViolationReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationReservationStore(
    var inner: ViolationReservationStore,
    var cache: TtlCache<ViolationReservation>,
    var hits: Int,
    var misses: Int,
) : ViolationReservationStore {
    override fun find(code: String): ViolationReservation? {
        val hit = (cache.get(code) as ViolationReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationReservation> = inner.list()
}

fun newCachedViolationReservationStore(inner: ViolationReservationStore, ttl: Int, clock: Clock): ViolationReservationStore {
    return CachedViolationReservationStore(
        inner = inner,
        cache = newTtlCache<ViolationReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation reservations. Empty fields match all. */
data class ViolationReservationQuery(
    var segment: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationReservation): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationReservation>): MutableList<ViolationReservation> {
        val matched = filter(items) { candidate: ViolationReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationReservations(store: ViolationReservationStore, query: ViolationReservationQuery): MutableList<ViolationReservation> {
    return query.apply(store.list())
}
