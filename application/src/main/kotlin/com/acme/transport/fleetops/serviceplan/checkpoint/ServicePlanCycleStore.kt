package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan cycles by code. */
interface ServicePlanCycleStore {
    fun find(code: String): ServicePlanCycle?
    fun save(item: ServicePlanCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanCycleStore(
    var items: MutableMap<String, ServicePlanCycle>,
    var order: MutableList<String>,
) : ServicePlanCycleStore {
    override fun find(code: String): ServicePlanCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanCycle) {
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

    override fun list(): MutableList<ServicePlanCycle> {
        val out = mutableListOf<ServicePlanCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanCycleStore(): ServicePlanCycleStore {
    return MemoryServicePlanCycleStore(mutableMapOf<String, ServicePlanCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanCycleStore(
    var inner: ServicePlanCycleStore,
    var cache: TtlCache<ServicePlanCycle>,
    var hits: Int,
    var misses: Int,
) : ServicePlanCycleStore {
    override fun find(code: String): ServicePlanCycle? {
        val hit = (cache.get(code) as ServicePlanCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanCycle> = inner.list()
}

fun newCachedServicePlanCycleStore(inner: ServicePlanCycleStore, ttl: Int, clock: Clock): ServicePlanCycleStore {
    return CachedServicePlanCycleStore(
        inner = inner,
        cache = newTtlCache<ServicePlanCycle>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan cycles. Empty fields match all. */
data class ServicePlanCycleQuery(
    var depot: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanCycle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanCycle>): MutableList<ServicePlanCycle> {
        val matched = filter(items) { candidate: ServicePlanCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanCycles(store: ServicePlanCycleStore, query: ServicePlanCycleQuery): MutableList<ServicePlanCycle> {
    return query.apply(store.list())
}
