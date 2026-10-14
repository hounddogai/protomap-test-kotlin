package com.acme.transport.emissions.idling.regionalidlingmanifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional idling reservations by code. */
interface RegionalIdlingReservationStore {
    fun find(code: String): RegionalIdlingReservation?
    fun save(item: RegionalIdlingReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalIdlingReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalIdlingReservationStore(
    var items: MutableMap<String, RegionalIdlingReservation>,
    var order: MutableList<String>,
) : RegionalIdlingReservationStore {
    override fun find(code: String): RegionalIdlingReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalIdlingReservation) {
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

    override fun list(): MutableList<RegionalIdlingReservation> {
        val out = mutableListOf<RegionalIdlingReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalIdlingReservationStore(): RegionalIdlingReservationStore {
    return MemoryRegionalIdlingReservationStore(
        mutableMapOf<String, RegionalIdlingReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalIdlingReservationStore(
    var inner: RegionalIdlingReservationStore,
    var cache: TtlCache<RegionalIdlingReservation>,
    var hits: Int,
    var misses: Int,
) : RegionalIdlingReservationStore {
    override fun find(code: String): RegionalIdlingReservation? {
        val hit = (cache.get(code) as RegionalIdlingReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalIdlingReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalIdlingReservation> = inner.list()
}

fun newCachedRegionalIdlingReservationStore(inner: RegionalIdlingReservationStore, ttl: Int, clock: Clock): RegionalIdlingReservationStore {
    return CachedRegionalIdlingReservationStore(
        inner = inner,
        cache = newTtlCache<RegionalIdlingReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional idling reservations. Empty fields match all. */
data class RegionalIdlingReservationQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalIdlingReservation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalIdlingReservation>): MutableList<RegionalIdlingReservation> {
        val matched = filter(items) { candidate: RegionalIdlingReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalIdlingReservations(store: RegionalIdlingReservationStore, query: RegionalIdlingReservationQuery): MutableList<RegionalIdlingReservation> {
    return query.apply(store.list())
}
