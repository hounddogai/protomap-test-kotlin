package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area plans by code. */
interface ServiceAreaPlanStore {
    fun find(code: String): ServiceAreaPlan?
    fun save(item: ServiceAreaPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaPlanStore(
    var items: MutableMap<String, ServiceAreaPlan>,
    var order: MutableList<String>,
) : ServiceAreaPlanStore {
    override fun find(code: String): ServiceAreaPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaPlan) {
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

    override fun list(): MutableList<ServiceAreaPlan> {
        val out = mutableListOf<ServiceAreaPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaPlanStore(): ServiceAreaPlanStore {
    return MemoryServiceAreaPlanStore(mutableMapOf<String, ServiceAreaPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaPlanStore(
    var inner: ServiceAreaPlanStore,
    var cache: TtlCache<ServiceAreaPlan>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaPlanStore {
    override fun find(code: String): ServiceAreaPlan? {
        val hit = (cache.get(code) as ServiceAreaPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaPlan> = inner.list()
}

fun newCachedServiceAreaPlanStore(inner: ServiceAreaPlanStore, ttl: Int, clock: Clock): ServiceAreaPlanStore {
    return CachedServiceAreaPlanStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaPlan>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area plans. Empty fields match all. */
data class ServiceAreaPlanQuery(
    var channel: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaPlan): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<ServiceAreaPlan>): MutableList<ServiceAreaPlan> {
        val matched = filter(items) { candidate: ServiceAreaPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaPlans(store: ServiceAreaPlanStore, query: ServiceAreaPlanQuery): MutableList<ServiceAreaPlan> {
    return query.apply(store.list())
}
