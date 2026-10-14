package com.acme.transport.lastmile.drop.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop reservations by code. */
interface DropReservationStore {
    fun find(code: String): DropReservation?
    fun save(item: DropReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropReservationStore(
    var items: MutableMap<String, DropReservation>,
    var order: MutableList<String>,
) : DropReservationStore {
    override fun find(code: String): DropReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropReservation) {
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

    override fun list(): MutableList<DropReservation> {
        val out = mutableListOf<DropReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropReservationStore(): DropReservationStore {
    return MemoryDropReservationStore(mutableMapOf<String, DropReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropReservationStore(
    var inner: DropReservationStore,
    var cache: TtlCache<DropReservation>,
    var hits: Int,
    var misses: Int,
) : DropReservationStore {
    override fun find(code: String): DropReservation? {
        val hit = (cache.get(code) as DropReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropReservation> = inner.list()
}

fun newCachedDropReservationStore(inner: DropReservationStore, ttl: Int, clock: Clock): DropReservationStore {
    return CachedDropReservationStore(
        inner = inner,
        cache = newTtlCache<DropReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop reservations. Empty fields match all. */
data class DropReservationQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropReservation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DropReservation>): MutableList<DropReservation> {
        val matched = filter(items) { candidate: DropReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropReservations(store: DropReservationStore, query: DropReservationQuery): MutableList<DropReservation> {
    return query.apply(store.list())
}
