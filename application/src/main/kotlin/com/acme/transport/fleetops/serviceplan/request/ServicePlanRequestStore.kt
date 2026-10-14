package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan requests by code. */
interface ServicePlanRequestStore {
    fun find(code: String): ServicePlanRequest?
    fun save(item: ServicePlanRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanRequestStore(
    var items: MutableMap<String, ServicePlanRequest>,
    var order: MutableList<String>,
) : ServicePlanRequestStore {
    override fun find(code: String): ServicePlanRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanRequest) {
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

    override fun list(): MutableList<ServicePlanRequest> {
        val out = mutableListOf<ServicePlanRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanRequestStore(): ServicePlanRequestStore {
    return MemoryServicePlanRequestStore(mutableMapOf<String, ServicePlanRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanRequestStore(
    var inner: ServicePlanRequestStore,
    var cache: TtlCache<ServicePlanRequest>,
    var hits: Int,
    var misses: Int,
) : ServicePlanRequestStore {
    override fun find(code: String): ServicePlanRequest? {
        val hit = (cache.get(code) as ServicePlanRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanRequest> = inner.list()
}

fun newCachedServicePlanRequestStore(inner: ServicePlanRequestStore, ttl: Int, clock: Clock): ServicePlanRequestStore {
    return CachedServicePlanRequestStore(
        inner = inner,
        cache = newTtlCache<ServicePlanRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan requests. Empty fields match all. */
data class ServicePlanRequestQuery(
    var source: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanRequest): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<ServicePlanRequest>): MutableList<ServicePlanRequest> {
        val matched = filter(items) { candidate: ServicePlanRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanRequests(store: ServicePlanRequestStore, query: ServicePlanRequestQuery): MutableList<ServicePlanRequest> {
    return query.apply(store.list())
}
