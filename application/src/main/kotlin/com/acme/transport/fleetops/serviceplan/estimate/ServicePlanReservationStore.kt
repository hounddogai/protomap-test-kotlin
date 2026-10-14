package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan reservations by code. */
interface ServicePlanReservationStore {
    fun find(code: String): ServicePlanReservation?
    fun save(item: ServicePlanReservation)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanReservation>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanReservationStore(
    var items: MutableMap<String, ServicePlanReservation>,
    var order: MutableList<String>,
) : ServicePlanReservationStore {
    override fun find(code: String): ServicePlanReservation? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanReservation) {
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

    override fun list(): MutableList<ServicePlanReservation> {
        val out = mutableListOf<ServicePlanReservation>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanReservationStore(): ServicePlanReservationStore {
    return MemoryServicePlanReservationStore(
        mutableMapOf<String, ServicePlanReservation>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanReservationStore(
    var inner: ServicePlanReservationStore,
    var cache: TtlCache<ServicePlanReservation>,
    var hits: Int,
    var misses: Int,
) : ServicePlanReservationStore {
    override fun find(code: String): ServicePlanReservation? {
        val hit = (cache.get(code) as ServicePlanReservation?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanReservation) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanReservation> = inner.list()
}

fun newCachedServicePlanReservationStore(inner: ServicePlanReservationStore, ttl: Int, clock: Clock): ServicePlanReservationStore {
    return CachedServicePlanReservationStore(
        inner = inner,
        cache = newTtlCache<ServicePlanReservation>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan reservations. Empty fields match all. */
data class ServicePlanReservationQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanReservation): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanReservation>): MutableList<ServicePlanReservation> {
        val matched = filter(items) { candidate: ServicePlanReservation -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanReservations(store: ServicePlanReservationStore, query: ServicePlanReservationQuery): MutableList<ServicePlanReservation> {
    return query.apply(store.list())
}
