package com.acme.transport.fleetops.serviceplan.planthreshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan adjustments by code. */
interface ServicePlanAdjustmentStore {
    fun find(code: String): ServicePlanAdjustment?
    fun save(item: ServicePlanAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanAdjustmentStore(
    var items: MutableMap<String, ServicePlanAdjustment>,
    var order: MutableList<String>,
) : ServicePlanAdjustmentStore {
    override fun find(code: String): ServicePlanAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanAdjustment) {
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

    override fun list(): MutableList<ServicePlanAdjustment> {
        val out = mutableListOf<ServicePlanAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanAdjustmentStore(): ServicePlanAdjustmentStore {
    return MemoryServicePlanAdjustmentStore(
        mutableMapOf<String, ServicePlanAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanAdjustmentStore(
    var inner: ServicePlanAdjustmentStore,
    var cache: TtlCache<ServicePlanAdjustment>,
    var hits: Int,
    var misses: Int,
) : ServicePlanAdjustmentStore {
    override fun find(code: String): ServicePlanAdjustment? {
        val hit = (cache.get(code) as ServicePlanAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanAdjustment> = inner.list()
}

fun newCachedServicePlanAdjustmentStore(inner: ServicePlanAdjustmentStore, ttl: Int, clock: Clock): ServicePlanAdjustmentStore {
    return CachedServicePlanAdjustmentStore(
        inner = inner,
        cache = newTtlCache<ServicePlanAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan adjustments. Empty fields match all. */
data class ServicePlanAdjustmentQuery(
    var category: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanAdjustment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanAdjustment>): MutableList<ServicePlanAdjustment> {
        val matched = filter(items) { candidate: ServicePlanAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanAdjustments(store: ServicePlanAdjustmentStore, query: ServicePlanAdjustmentQuery): MutableList<ServicePlanAdjustment> {
    return query.apply(store.list())
}
