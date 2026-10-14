package com.acme.transport.linehaul.relay.relayassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps manual relay reservations by code. */
interface ManualRelayReservationStore {
    fun find(code: String): ManualRelayReservation?
    fun save(item: ManualRelayReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualRelayReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualRelayReservationStore(
    var items: MutableMap<String, ManualRelayReservation>,
    var order: MutableList<String>,
) : ManualRelayReservationStore {
    override fun find(code: String): ManualRelayReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualRelayReservation) {
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

    override fun list(): MutableList<ManualRelayReservation> {
        val out = mutableListOf<ManualRelayReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualRelayReservationStore(): ManualRelayReservationStore {
    return MemoryManualRelayReservationStore(
        mutableMapOf<String, ManualRelayReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualRelayReservationStore(
    var inner: ManualRelayReservationStore,
    var cache: TtlCache<ManualRelayReservation>,
    var hits: Int,
    var misses: Int,
) : ManualRelayReservationStore {
    override fun find(code: String): ManualRelayReservation? {
        val hit = (cache.get(code) as ManualRelayReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualRelayReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualRelayReservation> = inner.list()
}

fun newCachedManualRelayReservationStore(inner: ManualRelayReservationStore, ttl: Int, clock: Clock): ManualRelayReservationStore {
    return CachedManualRelayReservationStore(
        inner = inner,
        cache = newTtlCache<ManualRelayReservation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual relay reservations. Empty fields match all. */
data class ManualRelayReservationQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualRelayReservation): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ManualRelayReservation>): MutableList<ManualRelayReservation> {
        val matched = filter(items) { candidate: ManualRelayReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualRelayReservations(store: ManualRelayReservationStore, query: ManualRelayReservationQuery): MutableList<ManualRelayReservation> {
    return query.apply(store.list())
}
