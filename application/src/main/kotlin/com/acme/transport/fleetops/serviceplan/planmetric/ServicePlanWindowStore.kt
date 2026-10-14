package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan windows by code. */
interface ServicePlanWindowStore {
    fun find(code: String): ServicePlanWindow?
    fun save(item: ServicePlanWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanWindowStore(
    var items: MutableMap<String, ServicePlanWindow>,
    var order: MutableList<String>,
) : ServicePlanWindowStore {
    override fun find(code: String): ServicePlanWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanWindow) {
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

    override fun list(): MutableList<ServicePlanWindow> {
        val out = mutableListOf<ServicePlanWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanWindowStore(): ServicePlanWindowStore {
    return MemoryServicePlanWindowStore(mutableMapOf<String, ServicePlanWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanWindowStore(
    var inner: ServicePlanWindowStore,
    var cache: TtlCache<ServicePlanWindow>,
    var hits: Int,
    var misses: Int,
) : ServicePlanWindowStore {
    override fun find(code: String): ServicePlanWindow? {
        val hit = (cache.get(code) as ServicePlanWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanWindow> = inner.list()
}

fun newCachedServicePlanWindowStore(inner: ServicePlanWindowStore, ttl: Int, clock: Clock): ServicePlanWindowStore {
    return CachedServicePlanWindowStore(
        inner = inner,
        cache = newTtlCache<ServicePlanWindow>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan windows. Empty fields match all. */
data class ServicePlanWindowQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ServicePlanWindow>): MutableList<ServicePlanWindow> {
        val matched = filter(items) { candidate: ServicePlanWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanWindows(store: ServicePlanWindowStore, query: ServicePlanWindowQuery): MutableList<ServicePlanWindow> {
    return query.apply(store.list())
}
