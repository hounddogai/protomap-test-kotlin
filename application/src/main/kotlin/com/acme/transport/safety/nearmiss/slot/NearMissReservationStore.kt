package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss reservations by code. */
interface NearMissReservationStore {
    fun find(code: String): NearMissReservation?
    fun save(item: NearMissReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissReservationStore(
    var items: MutableMap<String, NearMissReservation>,
    var order: MutableList<String>,
) : NearMissReservationStore {
    override fun find(code: String): NearMissReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissReservation) {
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

    override fun list(): MutableList<NearMissReservation> {
        val out = mutableListOf<NearMissReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissReservationStore(): NearMissReservationStore {
    return MemoryNearMissReservationStore(mutableMapOf<String, NearMissReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissReservationStore(
    var inner: NearMissReservationStore,
    var cache: TtlCache<NearMissReservation>,
    var hits: Int,
    var misses: Int,
) : NearMissReservationStore {
    override fun find(code: String): NearMissReservation? {
        val hit = (cache.get(code) as NearMissReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissReservation> = inner.list()
}

fun newCachedNearMissReservationStore(inner: NearMissReservationStore, ttl: Int, clock: Clock): NearMissReservationStore {
    return CachedNearMissReservationStore(
        inner = inner,
        cache = newTtlCache<NearMissReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss reservations. Empty fields match all. */
data class NearMissReservationQuery(
    var depot: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissReservation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissReservation>): MutableList<NearMissReservation> {
        val matched = filter(items) { candidate: NearMissReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissReservations(store: NearMissReservationStore, query: NearMissReservationQuery): MutableList<NearMissReservation> {
    return query.apply(store.list())
}
