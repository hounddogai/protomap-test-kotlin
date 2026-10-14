package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy trailer yard reservations by code. */
interface LegacyTrailerYardReservationStore {
    fun find(code: String): LegacyTrailerYardReservation?
    fun save(item: LegacyTrailerYardReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyTrailerYardReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyTrailerYardReservationStore(
    var items: MutableMap<String, LegacyTrailerYardReservation>,
    var order: MutableList<String>,
) : LegacyTrailerYardReservationStore {
    override fun find(code: String): LegacyTrailerYardReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyTrailerYardReservation) {
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

    override fun list(): MutableList<LegacyTrailerYardReservation> {
        val out = mutableListOf<LegacyTrailerYardReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyTrailerYardReservationStore(): LegacyTrailerYardReservationStore {
    return MemoryLegacyTrailerYardReservationStore(
        mutableMapOf<String, LegacyTrailerYardReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyTrailerYardReservationStore(
    var inner: LegacyTrailerYardReservationStore,
    var cache: TtlCache<LegacyTrailerYardReservation>,
    var hits: Int,
    var misses: Int,
) : LegacyTrailerYardReservationStore {
    override fun find(code: String): LegacyTrailerYardReservation? {
        val hit = (cache.get(code) as LegacyTrailerYardReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyTrailerYardReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyTrailerYardReservation> = inner.list()
}

fun newCachedLegacyTrailerYardReservationStore(inner: LegacyTrailerYardReservationStore, ttl: Int, clock: Clock): LegacyTrailerYardReservationStore {
    return CachedLegacyTrailerYardReservationStore(
        inner = inner,
        cache = newTtlCache<LegacyTrailerYardReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy trailer yard reservations. Empty fields match all. */
data class LegacyTrailerYardReservationQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyTrailerYardReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyTrailerYardReservation>): MutableList<LegacyTrailerYardReservation> {
        val matched = filter(items) { candidate: LegacyTrailerYardReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyTrailerYardReservations(store: LegacyTrailerYardReservationStore, query: LegacyTrailerYardReservationQuery): MutableList<LegacyTrailerYardReservation> {
    return query.apply(store.list())
}
