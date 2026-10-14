package com.acme.transport.terminal.crossdock.snapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock reservations by code. */
interface CrossDockReservationStore {
    fun find(code: String): CrossDockReservation?
    fun save(item: CrossDockReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockReservationStore(
    var items: MutableMap<String, CrossDockReservation>,
    var order: MutableList<String>,
) : CrossDockReservationStore {
    override fun find(code: String): CrossDockReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockReservation) {
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

    override fun list(): MutableList<CrossDockReservation> {
        val out = mutableListOf<CrossDockReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockReservationStore(): CrossDockReservationStore {
    return MemoryCrossDockReservationStore(
        mutableMapOf<String, CrossDockReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockReservationStore(
    var inner: CrossDockReservationStore,
    var cache: TtlCache<CrossDockReservation>,
    var hits: Int,
    var misses: Int,
) : CrossDockReservationStore {
    override fun find(code: String): CrossDockReservation? {
        val hit = (cache.get(code) as CrossDockReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockReservation> = inner.list()
}

fun newCachedCrossDockReservationStore(inner: CrossDockReservationStore, ttl: Int, clock: Clock): CrossDockReservationStore {
    return CachedCrossDockReservationStore(
        inner = inner,
        cache = newTtlCache<CrossDockReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock reservations. Empty fields match all. */
data class CrossDockReservationQuery(
    var depot: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockReservation): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockReservation>): MutableList<CrossDockReservation> {
        val matched = filter(items) { candidate: CrossDockReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockReservations(store: CrossDockReservationStore, query: CrossDockReservationQuery): MutableList<CrossDockReservation> {
    return query.apply(store.list())
}
