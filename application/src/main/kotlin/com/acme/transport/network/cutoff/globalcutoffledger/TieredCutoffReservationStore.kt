package com.acme.transport.network.cutoff.globalcutoffledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps tiered cutoff reservations by code. */
interface TieredCutoffReservationStore {
    fun find(code: String): TieredCutoffReservation?
    fun save(item: TieredCutoffReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCutoffReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCutoffReservationStore(
    var items: MutableMap<String, TieredCutoffReservation>,
    var order: MutableList<String>,
) : TieredCutoffReservationStore {
    override fun find(code: String): TieredCutoffReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCutoffReservation) {
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

    override fun list(): MutableList<TieredCutoffReservation> {
        val out = mutableListOf<TieredCutoffReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCutoffReservationStore(): TieredCutoffReservationStore {
    return MemoryTieredCutoffReservationStore(
        mutableMapOf<String, TieredCutoffReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCutoffReservationStore(
    var inner: TieredCutoffReservationStore,
    var cache: TtlCache<TieredCutoffReservation>,
    var hits: Int,
    var misses: Int,
) : TieredCutoffReservationStore {
    override fun find(code: String): TieredCutoffReservation? {
        val hit = (cache.get(code) as TieredCutoffReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCutoffReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCutoffReservation> = inner.list()
}

fun newCachedTieredCutoffReservationStore(inner: TieredCutoffReservationStore, ttl: Int, clock: Clock): TieredCutoffReservationStore {
    return CachedTieredCutoffReservationStore(
        inner = inner,
        cache = newTtlCache<TieredCutoffReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered cutoff reservations. Empty fields match all. */
data class TieredCutoffReservationQuery(
    var locale: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCutoffReservation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCutoffReservation>): MutableList<TieredCutoffReservation> {
        val matched = filter(items) { candidate: TieredCutoffReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCutoffReservations(store: TieredCutoffReservationStore, query: TieredCutoffReservationQuery): MutableList<TieredCutoffReservation> {
    return query.apply(store.list())
}
