package com.acme.transport.terminal.sortation.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default sortation reservations by code. */
interface DefaultSortationReservationStore {
    fun find(code: String): DefaultSortationReservation?
    fun save(item: DefaultSortationReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultSortationReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultSortationReservationStore(
    var items: MutableMap<String, DefaultSortationReservation>,
    var order: MutableList<String>,
) : DefaultSortationReservationStore {
    override fun find(code: String): DefaultSortationReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultSortationReservation) {
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

    override fun list(): MutableList<DefaultSortationReservation> {
        val out = mutableListOf<DefaultSortationReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultSortationReservationStore(): DefaultSortationReservationStore {
    return MemoryDefaultSortationReservationStore(
        mutableMapOf<String, DefaultSortationReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultSortationReservationStore(
    var inner: DefaultSortationReservationStore,
    var cache: TtlCache<DefaultSortationReservation>,
    var hits: Int,
    var misses: Int,
) : DefaultSortationReservationStore {
    override fun find(code: String): DefaultSortationReservation? {
        val hit = (cache.get(code) as DefaultSortationReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultSortationReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultSortationReservation> = inner.list()
}

fun newCachedDefaultSortationReservationStore(inner: DefaultSortationReservationStore, ttl: Int, clock: Clock): DefaultSortationReservationStore {
    return CachedDefaultSortationReservationStore(
        inner = inner,
        cache = newTtlCache<DefaultSortationReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default sortation reservations. Empty fields match all. */
data class DefaultSortationReservationQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultSortationReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultSortationReservation>): MutableList<DefaultSortationReservation> {
        val matched = filter(items) { candidate: DefaultSortationReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultSortationReservations(store: DefaultSortationReservationStore, query: DefaultSortationReservationQuery): MutableList<DefaultSortationReservation> {
    return query.apply(store.list())
}
