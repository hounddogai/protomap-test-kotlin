package com.acme.transport.fleetops.serviceplan.tier

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan baselines by code. */
interface ServicePlanBaselineStore {
    fun find(code: String): ServicePlanBaseline?
    fun save(item: ServicePlanBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanBaselineStore(
    var items: MutableMap<String, ServicePlanBaseline>,
    var order: MutableList<String>,
) : ServicePlanBaselineStore {
    override fun find(code: String): ServicePlanBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanBaseline) {
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

    override fun list(): MutableList<ServicePlanBaseline> {
        val out = mutableListOf<ServicePlanBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanBaselineStore(): ServicePlanBaselineStore {
    return MemoryServicePlanBaselineStore(mutableMapOf<String, ServicePlanBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanBaselineStore(
    var inner: ServicePlanBaselineStore,
    var cache: TtlCache<ServicePlanBaseline>,
    var hits: Int,
    var misses: Int,
) : ServicePlanBaselineStore {
    override fun find(code: String): ServicePlanBaseline? {
        val hit = (cache.get(code) as ServicePlanBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanBaseline> = inner.list()
}

fun newCachedServicePlanBaselineStore(inner: ServicePlanBaselineStore, ttl: Int, clock: Clock): ServicePlanBaselineStore {
    return CachedServicePlanBaselineStore(
        inner = inner,
        cache = newTtlCache<ServicePlanBaseline>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan baselines. Empty fields match all. */
data class ServicePlanBaselineQuery(
    var lane: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanBaseline): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<ServicePlanBaseline>): MutableList<ServicePlanBaseline> {
        val matched = filter(items) { candidate: ServicePlanBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanBaselines(store: ServicePlanBaselineStore, query: ServicePlanBaselineQuery): MutableList<ServicePlanBaseline> {
    return query.apply(store.list())
}
