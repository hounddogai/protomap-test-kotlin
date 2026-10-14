package com.acme.transport.fleetops.serviceplan.score

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan groups by code. */
interface ServicePlanGroupStore {
    fun find(code: String): ServicePlanGroup?
    fun save(item: ServicePlanGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanGroupStore(
    var items: MutableMap<String, ServicePlanGroup>,
    var order: MutableList<String>,
) : ServicePlanGroupStore {
    override fun find(code: String): ServicePlanGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanGroup) {
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

    override fun list(): MutableList<ServicePlanGroup> {
        val out = mutableListOf<ServicePlanGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanGroupStore(): ServicePlanGroupStore {
    return MemoryServicePlanGroupStore(mutableMapOf<String, ServicePlanGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanGroupStore(
    var inner: ServicePlanGroupStore,
    var cache: TtlCache<ServicePlanGroup>,
    var hits: Int,
    var misses: Int,
) : ServicePlanGroupStore {
    override fun find(code: String): ServicePlanGroup? {
        val hit = (cache.get(code) as ServicePlanGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanGroup> = inner.list()
}

fun newCachedServicePlanGroupStore(inner: ServicePlanGroupStore, ttl: Int, clock: Clock): ServicePlanGroupStore {
    return CachedServicePlanGroupStore(
        inner = inner,
        cache = newTtlCache<ServicePlanGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan groups. Empty fields match all. */
data class ServicePlanGroupQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanGroup>): MutableList<ServicePlanGroup> {
        val matched = filter(items) { candidate: ServicePlanGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanGroups(store: ServicePlanGroupStore, query: ServicePlanGroupQuery): MutableList<ServicePlanGroup> {
    return query.apply(store.list())
}
