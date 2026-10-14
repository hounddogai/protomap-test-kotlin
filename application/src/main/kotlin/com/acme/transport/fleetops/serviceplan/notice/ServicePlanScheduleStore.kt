package com.acme.transport.fleetops.serviceplan.notice

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan schedules by code. */
interface ServicePlanScheduleStore {
    fun find(code: String): ServicePlanSchedule?
    fun save(item: ServicePlanSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanScheduleStore(
    var items: MutableMap<String, ServicePlanSchedule>,
    var order: MutableList<String>,
) : ServicePlanScheduleStore {
    override fun find(code: String): ServicePlanSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanSchedule) {
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

    override fun list(): MutableList<ServicePlanSchedule> {
        val out = mutableListOf<ServicePlanSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanScheduleStore(): ServicePlanScheduleStore {
    return MemoryServicePlanScheduleStore(mutableMapOf<String, ServicePlanSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanScheduleStore(
    var inner: ServicePlanScheduleStore,
    var cache: TtlCache<ServicePlanSchedule>,
    var hits: Int,
    var misses: Int,
) : ServicePlanScheduleStore {
    override fun find(code: String): ServicePlanSchedule? {
        val hit = (cache.get(code) as ServicePlanSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanSchedule> = inner.list()
}

fun newCachedServicePlanScheduleStore(inner: ServicePlanScheduleStore, ttl: Int, clock: Clock): ServicePlanScheduleStore {
    return CachedServicePlanScheduleStore(
        inner = inner,
        cache = newTtlCache<ServicePlanSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan schedules. Empty fields match all. */
data class ServicePlanScheduleQuery(
    var locale: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanSchedule): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanSchedule>): MutableList<ServicePlanSchedule> {
        val matched = filter(items) { candidate: ServicePlanSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanSchedules(store: ServicePlanScheduleStore, query: ServicePlanScheduleQuery): MutableList<ServicePlanSchedule> {
    return query.apply(store.list())
}
