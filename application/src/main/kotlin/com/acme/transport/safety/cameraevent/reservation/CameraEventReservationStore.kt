package com.acme.transport.safety.cameraevent.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event reservations by code. */
interface CameraEventReservationStore {
    fun find(code: String): CameraEventReservation?
    fun save(item: CameraEventReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventReservationStore(
    var items: MutableMap<String, CameraEventReservation>,
    var order: MutableList<String>,
) : CameraEventReservationStore {
    override fun find(code: String): CameraEventReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventReservation) {
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

    override fun list(): MutableList<CameraEventReservation> {
        val out = mutableListOf<CameraEventReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventReservationStore(): CameraEventReservationStore {
    return MemoryCameraEventReservationStore(
        mutableMapOf<String, CameraEventReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventReservationStore(
    var inner: CameraEventReservationStore,
    var cache: TtlCache<CameraEventReservation>,
    var hits: Int,
    var misses: Int,
) : CameraEventReservationStore {
    override fun find(code: String): CameraEventReservation? {
        val hit = (cache.get(code) as CameraEventReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventReservation> = inner.list()
}

fun newCachedCameraEventReservationStore(inner: CameraEventReservationStore, ttl: Int, clock: Clock): CameraEventReservationStore {
    return CachedCameraEventReservationStore(
        inner = inner,
        cache = newTtlCache<CameraEventReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event reservations. Empty fields match all. */
data class CameraEventReservationQuery(
    var locale: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventReservation): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventReservation>): MutableList<CameraEventReservation> {
        val matched = filter(items) { candidate: CameraEventReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventReservations(store: CameraEventReservationStore, query: CameraEventReservationQuery): MutableList<CameraEventReservation> {
    return query.apply(store.list())
}
