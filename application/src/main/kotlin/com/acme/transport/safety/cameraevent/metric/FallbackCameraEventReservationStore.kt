package com.acme.transport.safety.cameraevent.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps fallback camera event reservations by code. */
interface FallbackCameraEventReservationStore {
    fun find(code: String): FallbackCameraEventReservation?
    fun save(item: FallbackCameraEventReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCameraEventReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCameraEventReservationStore(
    var items: MutableMap<String, FallbackCameraEventReservation>,
    var order: MutableList<String>,
) : FallbackCameraEventReservationStore {
    override fun find(code: String): FallbackCameraEventReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCameraEventReservation) {
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

    override fun list(): MutableList<FallbackCameraEventReservation> {
        val out = mutableListOf<FallbackCameraEventReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCameraEventReservationStore(): FallbackCameraEventReservationStore {
    return MemoryFallbackCameraEventReservationStore(
        mutableMapOf<String, FallbackCameraEventReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCameraEventReservationStore(
    var inner: FallbackCameraEventReservationStore,
    var cache: TtlCache<FallbackCameraEventReservation>,
    var hits: Int,
    var misses: Int,
) : FallbackCameraEventReservationStore {
    override fun find(code: String): FallbackCameraEventReservation? {
        val hit = (cache.get(code) as FallbackCameraEventReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCameraEventReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCameraEventReservation> = inner.list()
}

fun newCachedFallbackCameraEventReservationStore(inner: FallbackCameraEventReservationStore, ttl: Int, clock: Clock): FallbackCameraEventReservationStore {
    return CachedFallbackCameraEventReservationStore(
        inner = inner,
        cache = newTtlCache<FallbackCameraEventReservation>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback camera event reservations. Empty fields match all. */
data class FallbackCameraEventReservationQuery(
    var source: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCameraEventReservation): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackCameraEventReservation>): MutableList<FallbackCameraEventReservation> {
        val matched = filter(items) { candidate: FallbackCameraEventReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCameraEventReservations(store: FallbackCameraEventReservationStore, query: FallbackCameraEventReservationQuery): MutableList<FallbackCameraEventReservation> {
    return query.apply(store.list())
}
