package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending backhaul reservations by code. */
interface PendingBackhaulReservationStore {
    fun find(code: String): PendingBackhaulReservation?
    fun save(item: PendingBackhaulReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingBackhaulReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingBackhaulReservationStore(
    var items: MutableMap<String, PendingBackhaulReservation>,
    var order: MutableList<String>,
) : PendingBackhaulReservationStore {
    override fun find(code: String): PendingBackhaulReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingBackhaulReservation) {
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

    override fun list(): MutableList<PendingBackhaulReservation> {
        val out = mutableListOf<PendingBackhaulReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingBackhaulReservationStore(): PendingBackhaulReservationStore {
    return MemoryPendingBackhaulReservationStore(
        mutableMapOf<String, PendingBackhaulReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingBackhaulReservationStore(
    var inner: PendingBackhaulReservationStore,
    var cache: TtlCache<PendingBackhaulReservation>,
    var hits: Int,
    var misses: Int,
) : PendingBackhaulReservationStore {
    override fun find(code: String): PendingBackhaulReservation? {
        val hit = (cache.get(code) as PendingBackhaulReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingBackhaulReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingBackhaulReservation> = inner.list()
}

fun newCachedPendingBackhaulReservationStore(inner: PendingBackhaulReservationStore, ttl: Int, clock: Clock): PendingBackhaulReservationStore {
    return CachedPendingBackhaulReservationStore(
        inner = inner,
        cache = newTtlCache<PendingBackhaulReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending backhaul reservations. Empty fields match all. */
data class PendingBackhaulReservationQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingBackhaulReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingBackhaulReservation>): MutableList<PendingBackhaulReservation> {
        val matched = filter(items) { candidate: PendingBackhaulReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingBackhaulReservations(store: PendingBackhaulReservationStore, query: PendingBackhaulReservationQuery): MutableList<PendingBackhaulReservation> {
    return query.apply(store.list())
}
