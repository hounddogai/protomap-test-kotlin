package com.acme.transport.emissions.evcharge.snapshot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge reservations by code. */
interface EvChargeReservationStore {
    fun find(code: String): EvChargeReservation?
    fun save(item: EvChargeReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeReservationStore(
    var items: MutableMap<String, EvChargeReservation>,
    var order: MutableList<String>,
) : EvChargeReservationStore {
    override fun find(code: String): EvChargeReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeReservation) {
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

    override fun list(): MutableList<EvChargeReservation> {
        val out = mutableListOf<EvChargeReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeReservationStore(): EvChargeReservationStore {
    return MemoryEvChargeReservationStore(mutableMapOf<String, EvChargeReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeReservationStore(
    var inner: EvChargeReservationStore,
    var cache: TtlCache<EvChargeReservation>,
    var hits: Int,
    var misses: Int,
) : EvChargeReservationStore {
    override fun find(code: String): EvChargeReservation? {
        val hit = (cache.get(code) as EvChargeReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeReservation> = inner.list()
}

fun newCachedEvChargeReservationStore(inner: EvChargeReservationStore, ttl: Int, clock: Clock): EvChargeReservationStore {
    return CachedEvChargeReservationStore(
        inner = inner,
        cache = newTtlCache<EvChargeReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge reservations. Empty fields match all. */
data class EvChargeReservationQuery(
    var country: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeReservation): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeReservation>): MutableList<EvChargeReservation> {
        val matched = filter(items) { candidate: EvChargeReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeReservations(store: EvChargeReservationStore, query: EvChargeReservationQuery): MutableList<EvChargeReservation> {
    return query.apply(store.list())
}
