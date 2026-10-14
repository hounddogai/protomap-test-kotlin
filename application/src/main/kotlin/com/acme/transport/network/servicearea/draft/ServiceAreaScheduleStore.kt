package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area schedules by code. */
interface ServiceAreaScheduleStore {
    fun find(code: String): ServiceAreaSchedule?
    fun save(item: ServiceAreaSchedule)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaSchedule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaScheduleStore(
    var items: MutableMap<String, ServiceAreaSchedule>,
    var order: MutableList<String>,
) : ServiceAreaScheduleStore {
    override fun find(code: String): ServiceAreaSchedule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaSchedule) {
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

    override fun list(): MutableList<ServiceAreaSchedule> {
        val out = mutableListOf<ServiceAreaSchedule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaScheduleStore(): ServiceAreaScheduleStore {
    return MemoryServiceAreaScheduleStore(mutableMapOf<String, ServiceAreaSchedule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaScheduleStore(
    var inner: ServiceAreaScheduleStore,
    var cache: TtlCache<ServiceAreaSchedule>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaScheduleStore {
    override fun find(code: String): ServiceAreaSchedule? {
        val hit = (cache.get(code) as ServiceAreaSchedule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaSchedule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaSchedule> = inner.list()
}

fun newCachedServiceAreaScheduleStore(inner: ServiceAreaScheduleStore, ttl: Int, clock: Clock): ServiceAreaScheduleStore {
    return CachedServiceAreaScheduleStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaSchedule>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area schedules. Empty fields match all. */
data class ServiceAreaScheduleQuery(
    var region: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaSchedule): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<ServiceAreaSchedule>): MutableList<ServiceAreaSchedule> {
        val matched = filter(items) { candidate: ServiceAreaSchedule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaSchedules(store: ServiceAreaScheduleStore, query: ServiceAreaScheduleQuery): MutableList<ServiceAreaSchedule> {
    return query.apply(store.list())
}
