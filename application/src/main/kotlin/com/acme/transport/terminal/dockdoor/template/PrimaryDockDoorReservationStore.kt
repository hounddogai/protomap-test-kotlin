package com.acme.transport.terminal.dockdoor.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps primary dock door reservations by code. */
interface PrimaryDockDoorReservationStore {
    fun find(code: String): PrimaryDockDoorReservation?
    fun save(item: PrimaryDockDoorReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDockDoorReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDockDoorReservationStore(
    var items: MutableMap<String, PrimaryDockDoorReservation>,
    var order: MutableList<String>,
) : PrimaryDockDoorReservationStore {
    override fun find(code: String): PrimaryDockDoorReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDockDoorReservation) {
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

    override fun list(): MutableList<PrimaryDockDoorReservation> {
        val out = mutableListOf<PrimaryDockDoorReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDockDoorReservationStore(): PrimaryDockDoorReservationStore {
    return MemoryPrimaryDockDoorReservationStore(
        mutableMapOf<String, PrimaryDockDoorReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDockDoorReservationStore(
    var inner: PrimaryDockDoorReservationStore,
    var cache: TtlCache<PrimaryDockDoorReservation>,
    var hits: Int,
    var misses: Int,
) : PrimaryDockDoorReservationStore {
    override fun find(code: String): PrimaryDockDoorReservation? {
        val hit = (cache.get(code) as PrimaryDockDoorReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDockDoorReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDockDoorReservation> = inner.list()
}

fun newCachedPrimaryDockDoorReservationStore(inner: PrimaryDockDoorReservationStore, ttl: Int, clock: Clock): PrimaryDockDoorReservationStore {
    return CachedPrimaryDockDoorReservationStore(
        inner = inner,
        cache = newTtlCache<PrimaryDockDoorReservation>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary dock door reservations. Empty fields match all. */
data class PrimaryDockDoorReservationQuery(
    var segment: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDockDoorReservation): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryDockDoorReservation>): MutableList<PrimaryDockDoorReservation> {
        val matched = filter(items) { candidate: PrimaryDockDoorReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDockDoorReservations(store: PrimaryDockDoorReservationStore, query: PrimaryDockDoorReservationQuery): MutableList<PrimaryDockDoorReservation> {
    return query.apply(store.list())
}
