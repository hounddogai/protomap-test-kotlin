package com.acme.transport.fleetops.serviceplan.request

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan quotas by code. */
interface ServicePlanQuotaStore {
    fun find(code: String): ServicePlanQuota?
    fun save(item: ServicePlanQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanQuotaStore(
    var items: MutableMap<String, ServicePlanQuota>,
    var order: MutableList<String>,
) : ServicePlanQuotaStore {
    override fun find(code: String): ServicePlanQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanQuota) {
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

    override fun list(): MutableList<ServicePlanQuota> {
        val out = mutableListOf<ServicePlanQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanQuotaStore(): ServicePlanQuotaStore {
    return MemoryServicePlanQuotaStore(mutableMapOf<String, ServicePlanQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanQuotaStore(
    var inner: ServicePlanQuotaStore,
    var cache: TtlCache<ServicePlanQuota>,
    var hits: Int,
    var misses: Int,
) : ServicePlanQuotaStore {
    override fun find(code: String): ServicePlanQuota? {
        val hit = (cache.get(code) as ServicePlanQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanQuota> = inner.list()
}

fun newCachedServicePlanQuotaStore(inner: ServicePlanQuotaStore, ttl: Int, clock: Clock): ServicePlanQuotaStore {
    return CachedServicePlanQuotaStore(
        inner = inner,
        cache = newTtlCache<ServicePlanQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan quotas. Empty fields match all. */
data class ServicePlanQuotaQuery(
    var source: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanQuota): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanQuota>): MutableList<ServicePlanQuota> {
        val matched = filter(items) { candidate: ServicePlanQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanQuotas(store: ServicePlanQuotaStore, query: ServicePlanQuotaQuery): MutableList<ServicePlanQuota> {
    return query.apply(store.list())
}
