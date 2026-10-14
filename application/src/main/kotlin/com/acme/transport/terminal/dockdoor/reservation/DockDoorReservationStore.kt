package com.acme.transport.terminal.dockdoor.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door reservations by code. */
interface DockDoorReservationStore {
    fun find(code: String): DockDoorReservation?
    fun save(item: DockDoorReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorReservationStore(
    var items: MutableMap<String, DockDoorReservation>,
    var order: MutableList<String>,
) : DockDoorReservationStore {
    override fun find(code: String): DockDoorReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorReservation) {
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

    override fun list(): MutableList<DockDoorReservation> {
        val out = mutableListOf<DockDoorReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorReservationStore(): DockDoorReservationStore {
    return MemoryDockDoorReservationStore(mutableMapOf<String, DockDoorReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorReservationStore(
    var inner: DockDoorReservationStore,
    var cache: TtlCache<DockDoorReservation>,
    var hits: Int,
    var misses: Int,
) : DockDoorReservationStore {
    override fun find(code: String): DockDoorReservation? {
        val hit = (cache.get(code) as DockDoorReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorReservation> = inner.list()
}

fun newCachedDockDoorReservationStore(inner: DockDoorReservationStore, ttl: Int, clock: Clock): DockDoorReservationStore {
    return CachedDockDoorReservationStore(
        inner = inner,
        cache = newTtlCache<DockDoorReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door reservations. Empty fields match all. */
data class DockDoorReservationQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorReservation>): MutableList<DockDoorReservation> {
        val matched = filter(items) { candidate: DockDoorReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorReservations(store: DockDoorReservationStore, query: DockDoorReservationQuery): MutableList<DockDoorReservation> {
    return query.apply(store.list())
}
