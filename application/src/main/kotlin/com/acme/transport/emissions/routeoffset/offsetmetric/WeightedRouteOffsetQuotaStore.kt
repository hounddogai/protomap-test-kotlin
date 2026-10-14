package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted route offset quotas by code. */
interface WeightedRouteOffsetQuotaStore {
    fun find(code: String): WeightedRouteOffsetQuota?
    fun save(item: WeightedRouteOffsetQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRouteOffsetQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRouteOffsetQuotaStore(
    var items: MutableMap<String, WeightedRouteOffsetQuota>,
    var order: MutableList<String>,
) : WeightedRouteOffsetQuotaStore {
    override fun find(code: String): WeightedRouteOffsetQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRouteOffsetQuota) {
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

    override fun list(): MutableList<WeightedRouteOffsetQuota> {
        val out = mutableListOf<WeightedRouteOffsetQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRouteOffsetQuotaStore(): WeightedRouteOffsetQuotaStore {
    return MemoryWeightedRouteOffsetQuotaStore(
        mutableMapOf<String, WeightedRouteOffsetQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRouteOffsetQuotaStore(
    var inner: WeightedRouteOffsetQuotaStore,
    var cache: TtlCache<WeightedRouteOffsetQuota>,
    var hits: Int,
    var misses: Int,
) : WeightedRouteOffsetQuotaStore {
    override fun find(code: String): WeightedRouteOffsetQuota? {
        val hit = (cache.get(code) as WeightedRouteOffsetQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRouteOffsetQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRouteOffsetQuota> = inner.list()
}

fun newCachedWeightedRouteOffsetQuotaStore(inner: WeightedRouteOffsetQuotaStore, ttl: Int, clock: Clock): WeightedRouteOffsetQuotaStore {
    return CachedWeightedRouteOffsetQuotaStore(
        inner = inner,
        cache = newTtlCache<WeightedRouteOffsetQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted route offset quotas. Empty fields match all. */
data class WeightedRouteOffsetQuotaQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRouteOffsetQuota): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<WeightedRouteOffsetQuota>): MutableList<WeightedRouteOffsetQuota> {
        val matched = filter(items) { candidate: WeightedRouteOffsetQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRouteOffsetQuotas(store: WeightedRouteOffsetQuotaStore, query: WeightedRouteOffsetQuotaQuery): MutableList<WeightedRouteOffsetQuota> {
    return query.apply(store.list())
}
