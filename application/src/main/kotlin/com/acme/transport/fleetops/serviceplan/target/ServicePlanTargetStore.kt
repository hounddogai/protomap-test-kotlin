package com.acme.transport.fleetops.serviceplan.target

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan targets by code. */
interface ServicePlanTargetStore {
    fun find(code: String): ServicePlanTarget?
    fun save(item: ServicePlanTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanTargetStore(
    var items: MutableMap<String, ServicePlanTarget>,
    var order: MutableList<String>,
) : ServicePlanTargetStore {
    override fun find(code: String): ServicePlanTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanTarget) {
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

    override fun list(): MutableList<ServicePlanTarget> {
        val out = mutableListOf<ServicePlanTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanTargetStore(): ServicePlanTargetStore {
    return MemoryServicePlanTargetStore(mutableMapOf<String, ServicePlanTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanTargetStore(
    var inner: ServicePlanTargetStore,
    var cache: TtlCache<ServicePlanTarget>,
    var hits: Int,
    var misses: Int,
) : ServicePlanTargetStore {
    override fun find(code: String): ServicePlanTarget? {
        val hit = (cache.get(code) as ServicePlanTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanTarget> = inner.list()
}

fun newCachedServicePlanTargetStore(inner: ServicePlanTargetStore, ttl: Int, clock: Clock): ServicePlanTargetStore {
    return CachedServicePlanTargetStore(
        inner = inner,
        cache = newTtlCache<ServicePlanTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan targets. Empty fields match all. */
data class ServicePlanTargetQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanTarget>): MutableList<ServicePlanTarget> {
        val matched = filter(items) { candidate: ServicePlanTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanTargets(store: ServicePlanTargetStore, query: ServicePlanTargetQuery): MutableList<ServicePlanTarget> {
    return query.apply(store.list())
}
