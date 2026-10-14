package com.acme.transport.fleetops.serviceplan.estimate

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan claims by code. */
interface ServicePlanClaimStore {
    fun find(code: String): ServicePlanClaim?
    fun save(item: ServicePlanClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanClaimStore(
    var items: MutableMap<String, ServicePlanClaim>,
    var order: MutableList<String>,
) : ServicePlanClaimStore {
    override fun find(code: String): ServicePlanClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanClaim) {
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

    override fun list(): MutableList<ServicePlanClaim> {
        val out = mutableListOf<ServicePlanClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanClaimStore(): ServicePlanClaimStore {
    return MemoryServicePlanClaimStore(mutableMapOf<String, ServicePlanClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanClaimStore(
    var inner: ServicePlanClaimStore,
    var cache: TtlCache<ServicePlanClaim>,
    var hits: Int,
    var misses: Int,
) : ServicePlanClaimStore {
    override fun find(code: String): ServicePlanClaim? {
        val hit = (cache.get(code) as ServicePlanClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanClaim> = inner.list()
}

fun newCachedServicePlanClaimStore(inner: ServicePlanClaimStore, ttl: Int, clock: Clock): ServicePlanClaimStore {
    return CachedServicePlanClaimStore(
        inner = inner,
        cache = newTtlCache<ServicePlanClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan claims. Empty fields match all. */
data class ServicePlanClaimQuery(
    var country: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanClaim): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanClaim>): MutableList<ServicePlanClaim> {
        val matched = filter(items) { candidate: ServicePlanClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanClaims(store: ServicePlanClaimStore, query: ServicePlanClaimQuery): MutableList<ServicePlanClaim> {
    return query.apply(store.list())
}
