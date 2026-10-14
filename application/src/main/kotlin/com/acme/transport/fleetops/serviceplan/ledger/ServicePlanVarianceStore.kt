package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan variances by code. */
interface ServicePlanVarianceStore {
    fun find(code: String): ServicePlanVariance?
    fun save(item: ServicePlanVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanVarianceStore(
    var items: MutableMap<String, ServicePlanVariance>,
    var order: MutableList<String>,
) : ServicePlanVarianceStore {
    override fun find(code: String): ServicePlanVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanVariance) {
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

    override fun list(): MutableList<ServicePlanVariance> {
        val out = mutableListOf<ServicePlanVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanVarianceStore(): ServicePlanVarianceStore {
    return MemoryServicePlanVarianceStore(mutableMapOf<String, ServicePlanVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanVarianceStore(
    var inner: ServicePlanVarianceStore,
    var cache: TtlCache<ServicePlanVariance>,
    var hits: Int,
    var misses: Int,
) : ServicePlanVarianceStore {
    override fun find(code: String): ServicePlanVariance? {
        val hit = (cache.get(code) as ServicePlanVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanVariance> = inner.list()
}

fun newCachedServicePlanVarianceStore(inner: ServicePlanVarianceStore, ttl: Int, clock: Clock): ServicePlanVarianceStore {
    return CachedServicePlanVarianceStore(
        inner = inner,
        cache = newTtlCache<ServicePlanVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan variances. Empty fields match all. */
data class ServicePlanVarianceQuery(
    var source: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanVariance): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanVariance>): MutableList<ServicePlanVariance> {
        val matched = filter(items) { candidate: ServicePlanVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanVariances(store: ServicePlanVarianceStore, query: ServicePlanVarianceQuery): MutableList<ServicePlanVariance> {
    return query.apply(store.list())
}
