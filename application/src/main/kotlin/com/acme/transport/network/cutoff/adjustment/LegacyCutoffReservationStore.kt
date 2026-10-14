package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps legacy cutoff reservations by code. */
interface LegacyCutoffReservationStore {
    fun find(code: String): LegacyCutoffReservation?
    fun save(item: LegacyCutoffReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyCutoffReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyCutoffReservationStore(
    var items: MutableMap<String, LegacyCutoffReservation>,
    var order: MutableList<String>,
) : LegacyCutoffReservationStore {
    override fun find(code: String): LegacyCutoffReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyCutoffReservation) {
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

    override fun list(): MutableList<LegacyCutoffReservation> {
        val out = mutableListOf<LegacyCutoffReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyCutoffReservationStore(): LegacyCutoffReservationStore {
    return MemoryLegacyCutoffReservationStore(
        mutableMapOf<String, LegacyCutoffReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyCutoffReservationStore(
    var inner: LegacyCutoffReservationStore,
    var cache: TtlCache<LegacyCutoffReservation>,
    var hits: Int,
    var misses: Int,
) : LegacyCutoffReservationStore {
    override fun find(code: String): LegacyCutoffReservation? {
        val hit = (cache.get(code) as LegacyCutoffReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyCutoffReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyCutoffReservation> = inner.list()
}

fun newCachedLegacyCutoffReservationStore(inner: LegacyCutoffReservationStore, ttl: Int, clock: Clock): LegacyCutoffReservationStore {
    return CachedLegacyCutoffReservationStore(
        inner = inner,
        cache = newTtlCache<LegacyCutoffReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy cutoff reservations. Empty fields match all. */
data class LegacyCutoffReservationQuery(
    var label: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyCutoffReservation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyCutoffReservation>): MutableList<LegacyCutoffReservation> {
        val matched = filter(items) { candidate: LegacyCutoffReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyCutoffReservations(store: LegacyCutoffReservationStore, query: LegacyCutoffReservationQuery): MutableList<LegacyCutoffReservation> {
    return query.apply(store.list())
}
