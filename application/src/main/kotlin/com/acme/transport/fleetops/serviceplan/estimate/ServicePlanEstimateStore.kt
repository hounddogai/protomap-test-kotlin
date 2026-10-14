package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan estimates by code. */
interface ServicePlanEstimateStore {
    fun find(code: String): ServicePlanEstimate?
    fun save(item: ServicePlanEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanEstimateStore(
    var items: MutableMap<String, ServicePlanEstimate>,
    var order: MutableList<String>,
) : ServicePlanEstimateStore {
    override fun find(code: String): ServicePlanEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanEstimate) {
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

    override fun list(): MutableList<ServicePlanEstimate> {
        val out = mutableListOf<ServicePlanEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanEstimateStore(): ServicePlanEstimateStore {
    return MemoryServicePlanEstimateStore(mutableMapOf<String, ServicePlanEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanEstimateStore(
    var inner: ServicePlanEstimateStore,
    var cache: TtlCache<ServicePlanEstimate>,
    var hits: Int,
    var misses: Int,
) : ServicePlanEstimateStore {
    override fun find(code: String): ServicePlanEstimate? {
        val hit = (cache.get(code) as ServicePlanEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanEstimate> = inner.list()
}

fun newCachedServicePlanEstimateStore(inner: ServicePlanEstimateStore, ttl: Int, clock: Clock): ServicePlanEstimateStore {
    return CachedServicePlanEstimateStore(
        inner = inner,
        cache = newTtlCache<ServicePlanEstimate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan estimates. Empty fields match all. */
data class ServicePlanEstimateQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanEstimate>): MutableList<ServicePlanEstimate> {
        val matched = filter(items) { candidate: ServicePlanEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanEstimates(store: ServicePlanEstimateStore, query: ServicePlanEstimateQuery): MutableList<ServicePlanEstimate> {
    return query.apply(store.list())
}
