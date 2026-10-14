package com.acme.transport.fleetops.serviceplan.threshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan thresholds by code. */
interface ServicePlanThresholdStore {
    fun find(code: String): ServicePlanThreshold?
    fun save(item: ServicePlanThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanThresholdStore(
    var items: MutableMap<String, ServicePlanThreshold>,
    var order: MutableList<String>,
) : ServicePlanThresholdStore {
    override fun find(code: String): ServicePlanThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanThreshold) {
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

    override fun list(): MutableList<ServicePlanThreshold> {
        val out = mutableListOf<ServicePlanThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanThresholdStore(): ServicePlanThresholdStore {
    return MemoryServicePlanThresholdStore(
        mutableMapOf<String, ServicePlanThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanThresholdStore(
    var inner: ServicePlanThresholdStore,
    var cache: TtlCache<ServicePlanThreshold>,
    var hits: Int,
    var misses: Int,
) : ServicePlanThresholdStore {
    override fun find(code: String): ServicePlanThreshold? {
        val hit = (cache.get(code) as ServicePlanThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanThreshold> = inner.list()
}

fun newCachedServicePlanThresholdStore(inner: ServicePlanThresholdStore, ttl: Int, clock: Clock): ServicePlanThresholdStore {
    return CachedServicePlanThresholdStore(
        inner = inner,
        cache = newTtlCache<ServicePlanThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan thresholds. Empty fields match all. */
data class ServicePlanThresholdQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ServicePlanThreshold>): MutableList<ServicePlanThreshold> {
        val matched = filter(items) { candidate: ServicePlanThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanThresholds(store: ServicePlanThresholdStore, query: ServicePlanThresholdQuery): MutableList<ServicePlanThreshold> {
    return query.apply(store.list())
}
