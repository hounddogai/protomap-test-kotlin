package com.acme.transport.linehaul.relay.notice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay reservations by code. */
interface RelayReservationStore {
    fun find(code: String): RelayReservation?
    fun save(item: RelayReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayReservationStore(
    var items: MutableMap<String, RelayReservation>,
    var order: MutableList<String>,
) : RelayReservationStore {
    override fun find(code: String): RelayReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayReservation) {
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

    override fun list(): MutableList<RelayReservation> {
        val out = mutableListOf<RelayReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayReservationStore(): RelayReservationStore {
    return MemoryRelayReservationStore(mutableMapOf<String, RelayReservation>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayReservationStore(
    var inner: RelayReservationStore,
    var cache: TtlCache<RelayReservation>,
    var hits: Int,
    var misses: Int,
) : RelayReservationStore {
    override fun find(code: String): RelayReservation? {
        val hit = (cache.get(code) as RelayReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayReservation> = inner.list()
}

fun newCachedRelayReservationStore(inner: RelayReservationStore, ttl: Int, clock: Clock): RelayReservationStore {
    return CachedRelayReservationStore(
        inner = inner,
        cache = newTtlCache<RelayReservation>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay reservations. Empty fields match all. */
data class RelayReservationQuery(
    var channel: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayReservation): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RelayReservation>): MutableList<RelayReservation> {
        val matched = filter(items) { candidate: RelayReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayReservations(store: RelayReservationStore, query: RelayReservationQuery): MutableList<RelayReservation> {
    return query.apply(store.list())
}
