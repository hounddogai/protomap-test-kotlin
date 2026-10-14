package com.acme.transport.network.servicearea.areaassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area quotas by code. */
interface ServiceAreaQuotaStore {
    fun find(code: String): ServiceAreaQuota?
    fun save(item: ServiceAreaQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaQuotaStore(
    var items: MutableMap<String, ServiceAreaQuota>,
    var order: MutableList<String>,
) : ServiceAreaQuotaStore {
    override fun find(code: String): ServiceAreaQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaQuota) {
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

    override fun list(): MutableList<ServiceAreaQuota> {
        val out = mutableListOf<ServiceAreaQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaQuotaStore(): ServiceAreaQuotaStore {
    return MemoryServiceAreaQuotaStore(mutableMapOf<String, ServiceAreaQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaQuotaStore(
    var inner: ServiceAreaQuotaStore,
    var cache: TtlCache<ServiceAreaQuota>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaQuotaStore {
    override fun find(code: String): ServiceAreaQuota? {
        val hit = (cache.get(code) as ServiceAreaQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaQuota> = inner.list()
}

fun newCachedServiceAreaQuotaStore(inner: ServiceAreaQuotaStore, ttl: Int, clock: Clock): ServiceAreaQuotaStore {
    return CachedServiceAreaQuotaStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area quotas. Empty fields match all. */
data class ServiceAreaQuotaQuery(
    var category: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaQuota): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaQuota>): MutableList<ServiceAreaQuota> {
        val matched = filter(items) { candidate: ServiceAreaQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaQuotas(store: ServiceAreaQuotaStore, query: ServiceAreaQuotaQuery): MutableList<ServiceAreaQuota> {
    return query.apply(store.list())
}
