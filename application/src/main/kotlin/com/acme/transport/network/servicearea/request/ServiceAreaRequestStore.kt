package com.acme.transport.network.servicearea.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area requests by code. */
interface ServiceAreaRequestStore {
    fun find(code: String): ServiceAreaRequest?
    fun save(item: ServiceAreaRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaRequestStore(
    var items: MutableMap<String, ServiceAreaRequest>,
    var order: MutableList<String>,
) : ServiceAreaRequestStore {
    override fun find(code: String): ServiceAreaRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaRequest) {
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

    override fun list(): MutableList<ServiceAreaRequest> {
        val out = mutableListOf<ServiceAreaRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaRequestStore(): ServiceAreaRequestStore {
    return MemoryServiceAreaRequestStore(mutableMapOf<String, ServiceAreaRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaRequestStore(
    var inner: ServiceAreaRequestStore,
    var cache: TtlCache<ServiceAreaRequest>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaRequestStore {
    override fun find(code: String): ServiceAreaRequest? {
        val hit = (cache.get(code) as ServiceAreaRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaRequest> = inner.list()
}

fun newCachedServiceAreaRequestStore(inner: ServiceAreaRequestStore, ttl: Int, clock: Clock): ServiceAreaRequestStore {
    return CachedServiceAreaRequestStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaRequest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area requests. Empty fields match all. */
data class ServiceAreaRequestQuery(
    var lane: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaRequest): Boolean {
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

    fun apply(items: MutableList<ServiceAreaRequest>): MutableList<ServiceAreaRequest> {
        val matched = filter(items) { candidate: ServiceAreaRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaRequests(store: ServiceAreaRequestStore, query: ServiceAreaRequestQuery): MutableList<ServiceAreaRequest> {
    return query.apply(store.list())
}
