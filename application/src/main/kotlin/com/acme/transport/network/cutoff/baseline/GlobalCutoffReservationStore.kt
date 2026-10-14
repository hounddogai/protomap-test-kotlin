package com.acme.transport.network.cutoff.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global cutoff reservations by code. */
interface GlobalCutoffReservationStore {
    fun find(code: String): GlobalCutoffReservation?
    fun save(item: GlobalCutoffReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCutoffReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCutoffReservationStore(
    var items: MutableMap<String, GlobalCutoffReservation>,
    var order: MutableList<String>,
) : GlobalCutoffReservationStore {
    override fun find(code: String): GlobalCutoffReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCutoffReservation) {
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

    override fun list(): MutableList<GlobalCutoffReservation> {
        val out = mutableListOf<GlobalCutoffReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCutoffReservationStore(): GlobalCutoffReservationStore {
    return MemoryGlobalCutoffReservationStore(
        mutableMapOf<String, GlobalCutoffReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCutoffReservationStore(
    var inner: GlobalCutoffReservationStore,
    var cache: TtlCache<GlobalCutoffReservation>,
    var hits: Int,
    var misses: Int,
) : GlobalCutoffReservationStore {
    override fun find(code: String): GlobalCutoffReservation? {
        val hit = (cache.get(code) as GlobalCutoffReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCutoffReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCutoffReservation> = inner.list()
}

fun newCachedGlobalCutoffReservationStore(inner: GlobalCutoffReservationStore, ttl: Int, clock: Clock): GlobalCutoffReservationStore {
    return CachedGlobalCutoffReservationStore(
        inner = inner,
        cache = newTtlCache<GlobalCutoffReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global cutoff reservations. Empty fields match all. */
data class GlobalCutoffReservationQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCutoffReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalCutoffReservation>): MutableList<GlobalCutoffReservation> {
        val matched = filter(items) { candidate: GlobalCutoffReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCutoffReservations(store: GlobalCutoffReservationStore, query: GlobalCutoffReservationQuery): MutableList<GlobalCutoffReservation> {
    return query.apply(store.list())
}
