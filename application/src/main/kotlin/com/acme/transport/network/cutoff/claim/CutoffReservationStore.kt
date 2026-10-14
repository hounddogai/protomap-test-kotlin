package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff reservations by code. */
interface CutoffReservationStore {
    fun find(code: String): CutoffReservation?
    fun save(item: CutoffReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffReservationStore(
    var items: MutableMap<String, CutoffReservation>,
    var order: MutableList<String>,
) : CutoffReservationStore {
    override fun find(code: String): CutoffReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffReservation) {
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

    override fun list(): MutableList<CutoffReservation> {
        val out = mutableListOf<CutoffReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffReservationStore(): CutoffReservationStore {
    return MemoryCutoffReservationStore(mutableMapOf<String, CutoffReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffReservationStore(
    var inner: CutoffReservationStore,
    var cache: TtlCache<CutoffReservation>,
    var hits: Int,
    var misses: Int,
) : CutoffReservationStore {
    override fun find(code: String): CutoffReservation? {
        val hit = (cache.get(code) as CutoffReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffReservation> = inner.list()
}

fun newCachedCutoffReservationStore(inner: CutoffReservationStore, ttl: Int, clock: Clock): CutoffReservationStore {
    return CachedCutoffReservationStore(
        inner = inner,
        cache = newTtlCache<CutoffReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff reservations. Empty fields match all. */
data class CutoffReservationQuery(
    var category: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffReservation): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CutoffReservation>): MutableList<CutoffReservation> {
        val matched = filter(items) { candidate: CutoffReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffReservations(store: CutoffReservationStore, query: CutoffReservationQuery): MutableList<CutoffReservation> {
    return query.apply(store.list())
}
