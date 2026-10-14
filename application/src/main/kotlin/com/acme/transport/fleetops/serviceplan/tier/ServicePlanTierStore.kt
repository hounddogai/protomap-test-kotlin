package com.acme.transport.fleetops.serviceplan.tier

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan tiers by code. */
interface ServicePlanTierStore {
    fun find(code: String): ServicePlanTier?
    fun save(item: ServicePlanTier)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanTier>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanTierStore(
    var items: MutableMap<String, ServicePlanTier>,
    var order: MutableList<String>,
) : ServicePlanTierStore {
    override fun find(code: String): ServicePlanTier? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanTier) {
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

    override fun list(): MutableList<ServicePlanTier> {
        val out = mutableListOf<ServicePlanTier>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanTierStore(): ServicePlanTierStore {
    return MemoryServicePlanTierStore(mutableMapOf<String, ServicePlanTier>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanTierStore(
    var inner: ServicePlanTierStore,
    var cache: TtlCache<ServicePlanTier>,
    var hits: Int,
    var misses: Int,
) : ServicePlanTierStore {
    override fun find(code: String): ServicePlanTier? {
        val hit = (cache.get(code) as ServicePlanTier?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanTier) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanTier> = inner.list()
}

fun newCachedServicePlanTierStore(inner: ServicePlanTierStore, ttl: Int, clock: Clock): ServicePlanTierStore {
    return CachedServicePlanTierStore(
        inner = inner,
        cache = newTtlCache<ServicePlanTier>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan tiers. Empty fields match all. */
data class ServicePlanTierQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanTier): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ServicePlanTier>): MutableList<ServicePlanTier> {
        val matched = filter(items) { candidate: ServicePlanTier -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanTiers(store: ServicePlanTierStore, query: ServicePlanTierQuery): MutableList<ServicePlanTier> {
    return query.apply(store.list())
}
