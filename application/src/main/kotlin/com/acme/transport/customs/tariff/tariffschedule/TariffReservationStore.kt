package com.acme.transport.customs.tariff.tariffschedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff reservations by code. */
interface TariffReservationStore {
    fun find(code: String): TariffReservation?
    fun save(item: TariffReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffReservationStore(
    var items: MutableMap<String, TariffReservation>,
    var order: MutableList<String>,
) : TariffReservationStore {
    override fun find(code: String): TariffReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffReservation) {
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

    override fun list(): MutableList<TariffReservation> {
        val out = mutableListOf<TariffReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffReservationStore(): TariffReservationStore {
    return MemoryTariffReservationStore(mutableMapOf<String, TariffReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffReservationStore(
    var inner: TariffReservationStore,
    var cache: TtlCache<TariffReservation>,
    var hits: Int,
    var misses: Int,
) : TariffReservationStore {
    override fun find(code: String): TariffReservation? {
        val hit = (cache.get(code) as TariffReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffReservation> = inner.list()
}

fun newCachedTariffReservationStore(inner: TariffReservationStore, ttl: Int, clock: Clock): TariffReservationStore {
    return CachedTariffReservationStore(
        inner = inner,
        cache = newTtlCache<TariffReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff reservations. Empty fields match all. */
data class TariffReservationQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffReservation>): MutableList<TariffReservation> {
        val matched = filter(items) { candidate: TariffReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffReservations(store: TariffReservationStore, query: TariffReservationQuery): MutableList<TariffReservation> {
    return query.apply(store.list())
}
