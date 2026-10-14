package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps legacy backhaul reservations by code. */
interface LegacyBackhaulReservationStore {
    fun find(code: String): LegacyBackhaulReservation?
    fun save(item: LegacyBackhaulReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyBackhaulReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyBackhaulReservationStore(
    var items: MutableMap<String, LegacyBackhaulReservation>,
    var order: MutableList<String>,
) : LegacyBackhaulReservationStore {
    override fun find(code: String): LegacyBackhaulReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyBackhaulReservation) {
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

    override fun list(): MutableList<LegacyBackhaulReservation> {
        val out = mutableListOf<LegacyBackhaulReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyBackhaulReservationStore(): LegacyBackhaulReservationStore {
    return MemoryLegacyBackhaulReservationStore(
        mutableMapOf<String, LegacyBackhaulReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyBackhaulReservationStore(
    var inner: LegacyBackhaulReservationStore,
    var cache: TtlCache<LegacyBackhaulReservation>,
    var hits: Int,
    var misses: Int,
) : LegacyBackhaulReservationStore {
    override fun find(code: String): LegacyBackhaulReservation? {
        val hit = (cache.get(code) as LegacyBackhaulReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyBackhaulReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyBackhaulReservation> = inner.list()
}

fun newCachedLegacyBackhaulReservationStore(inner: LegacyBackhaulReservationStore, ttl: Int, clock: Clock): LegacyBackhaulReservationStore {
    return CachedLegacyBackhaulReservationStore(
        inner = inner,
        cache = newTtlCache<LegacyBackhaulReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy backhaul reservations. Empty fields match all. */
data class LegacyBackhaulReservationQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyBackhaulReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyBackhaulReservation>): MutableList<LegacyBackhaulReservation> {
        val matched = filter(items) { candidate: LegacyBackhaulReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyBackhaulReservations(store: LegacyBackhaulReservationStore, query: LegacyBackhaulReservationQuery): MutableList<LegacyBackhaulReservation> {
    return query.apply(store.list())
}
