package com.acme.transport.emissions.evcharge.quota

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted ev charge quotas by code. */
interface WeightedEvChargeQuotaStore {
    fun find(code: String): WeightedEvChargeQuota?
    fun save(item: WeightedEvChargeQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedEvChargeQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedEvChargeQuotaStore(
    var items: MutableMap<String, WeightedEvChargeQuota>,
    var order: MutableList<String>,
) : WeightedEvChargeQuotaStore {
    override fun find(code: String): WeightedEvChargeQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedEvChargeQuota) {
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

    override fun list(): MutableList<WeightedEvChargeQuota> {
        val out = mutableListOf<WeightedEvChargeQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedEvChargeQuotaStore(): WeightedEvChargeQuotaStore {
    return MemoryWeightedEvChargeQuotaStore(
        mutableMapOf<String, WeightedEvChargeQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedEvChargeQuotaStore(
    var inner: WeightedEvChargeQuotaStore,
    var cache: TtlCache<WeightedEvChargeQuota>,
    var hits: Int,
    var misses: Int,
) : WeightedEvChargeQuotaStore {
    override fun find(code: String): WeightedEvChargeQuota? {
        val hit = (cache.get(code) as WeightedEvChargeQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedEvChargeQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedEvChargeQuota> = inner.list()
}

fun newCachedWeightedEvChargeQuotaStore(inner: WeightedEvChargeQuotaStore, ttl: Int, clock: Clock): WeightedEvChargeQuotaStore {
    return CachedWeightedEvChargeQuotaStore(
        inner = inner,
        cache = newTtlCache<WeightedEvChargeQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted ev charge quotas. Empty fields match all. */
data class WeightedEvChargeQuotaQuery(
    var source: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedEvChargeQuota): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<WeightedEvChargeQuota>): MutableList<WeightedEvChargeQuota> {
        val matched = filter(items) { candidate: WeightedEvChargeQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedEvChargeQuotas(store: WeightedEvChargeQuotaStore, query: WeightedEvChargeQuotaQuery): MutableList<WeightedEvChargeQuota> {
    return query.apply(store.list())
}
