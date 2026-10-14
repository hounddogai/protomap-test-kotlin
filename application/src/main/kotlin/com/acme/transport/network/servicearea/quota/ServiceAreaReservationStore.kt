package com.acme.transport.network.servicearea.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area reservations by code. */
interface ServiceAreaReservationStore {
    fun find(code: String): ServiceAreaReservation?
    fun save(item: ServiceAreaReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaReservationStore(
    var items: MutableMap<String, ServiceAreaReservation>,
    var order: MutableList<String>,
) : ServiceAreaReservationStore {
    override fun find(code: String): ServiceAreaReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaReservation) {
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

    override fun list(): MutableList<ServiceAreaReservation> {
        val out = mutableListOf<ServiceAreaReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaReservationStore(): ServiceAreaReservationStore {
    return MemoryServiceAreaReservationStore(
        mutableMapOf<String, ServiceAreaReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaReservationStore(
    var inner: ServiceAreaReservationStore,
    var cache: TtlCache<ServiceAreaReservation>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaReservationStore {
    override fun find(code: String): ServiceAreaReservation? {
        val hit = (cache.get(code) as ServiceAreaReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaReservation> = inner.list()
}

fun newCachedServiceAreaReservationStore(inner: ServiceAreaReservationStore, ttl: Int, clock: Clock): ServiceAreaReservationStore {
    return CachedServiceAreaReservationStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaReservation>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area reservations. Empty fields match all. */
data class ServiceAreaReservationQuery(
    var label: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaReservation): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaReservation>): MutableList<ServiceAreaReservation> {
        val matched = filter(items) { candidate: ServiceAreaReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaReservations(store: ServiceAreaReservationStore, query: ServiceAreaReservationQuery): MutableList<ServiceAreaReservation> {
    return query.apply(store.list())
}
