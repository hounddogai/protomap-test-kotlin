package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation reservations by code. */
interface SortationReservationStore {
    fun find(code: String): SortationReservation?
    fun save(item: SortationReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationReservationStore(
    var items: MutableMap<String, SortationReservation>,
    var order: MutableList<String>,
) : SortationReservationStore {
    override fun find(code: String): SortationReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationReservation) {
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

    override fun list(): MutableList<SortationReservation> {
        val out = mutableListOf<SortationReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationReservationStore(): SortationReservationStore {
    return MemorySortationReservationStore(
        mutableMapOf<String, SortationReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationReservationStore(
    var inner: SortationReservationStore,
    var cache: TtlCache<SortationReservation>,
    var hits: Int,
    var misses: Int,
) : SortationReservationStore {
    override fun find(code: String): SortationReservation? {
        val hit = (cache.get(code) as SortationReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationReservation> = inner.list()
}

fun newCachedSortationReservationStore(inner: SortationReservationStore, ttl: Int, clock: Clock): SortationReservationStore {
    return CachedSortationReservationStore(
        inner = inner,
        cache = newTtlCache<SortationReservation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation reservations. Empty fields match all. */
data class SortationReservationQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationReservation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<SortationReservation>): MutableList<SortationReservation> {
        val matched = filter(items) { candidate: SortationReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationReservations(store: SortationReservationStore, query: SortationReservationQuery): MutableList<SortationReservation> {
    return query.apply(store.list())
}
