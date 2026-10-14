package com.acme.transport.network.servicearea.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps regional service area reservations by code. */
interface RegionalServiceAreaReservationStore {
    fun find(code: String): RegionalServiceAreaReservation?
    fun save(item: RegionalServiceAreaReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalServiceAreaReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalServiceAreaReservationStore(
    var items: MutableMap<String, RegionalServiceAreaReservation>,
    var order: MutableList<String>,
) : RegionalServiceAreaReservationStore {
    override fun find(code: String): RegionalServiceAreaReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalServiceAreaReservation) {
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

    override fun list(): MutableList<RegionalServiceAreaReservation> {
        val out = mutableListOf<RegionalServiceAreaReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalServiceAreaReservationStore(): RegionalServiceAreaReservationStore {
    return MemoryRegionalServiceAreaReservationStore(
        mutableMapOf<String, RegionalServiceAreaReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalServiceAreaReservationStore(
    var inner: RegionalServiceAreaReservationStore,
    var cache: TtlCache<RegionalServiceAreaReservation>,
    var hits: Int,
    var misses: Int,
) : RegionalServiceAreaReservationStore {
    override fun find(code: String): RegionalServiceAreaReservation? {
        val hit = (cache.get(code) as RegionalServiceAreaReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalServiceAreaReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalServiceAreaReservation> = inner.list()
}

fun newCachedRegionalServiceAreaReservationStore(inner: RegionalServiceAreaReservationStore, ttl: Int, clock: Clock): RegionalServiceAreaReservationStore {
    return CachedRegionalServiceAreaReservationStore(
        inner = inner,
        cache = newTtlCache<RegionalServiceAreaReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional service area reservations. Empty fields match all. */
data class RegionalServiceAreaReservationQuery(
    var channel: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalServiceAreaReservation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalServiceAreaReservation>): MutableList<RegionalServiceAreaReservation> {
        val matched = filter(items) { candidate: RegionalServiceAreaReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalServiceAreaReservations(store: RegionalServiceAreaReservationStore, query: RegionalServiceAreaReservationQuery): MutableList<RegionalServiceAreaReservation> {
    return query.apply(store.list())
}
