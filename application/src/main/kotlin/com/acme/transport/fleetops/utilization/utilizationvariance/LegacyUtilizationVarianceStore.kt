package com.acme.transport.fleetops.utilization.utilizationvariance

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy utilization variances by code. */
interface LegacyUtilizationVarianceStore {
    fun find(code: String): LegacyUtilizationVariance?
    fun save(item: LegacyUtilizationVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyUtilizationVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyUtilizationVarianceStore(
    var items: MutableMap<String, LegacyUtilizationVariance>,
    var order: MutableList<String>,
) : LegacyUtilizationVarianceStore {
    override fun find(code: String): LegacyUtilizationVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyUtilizationVariance) {
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

    override fun list(): MutableList<LegacyUtilizationVariance> {
        val out = mutableListOf<LegacyUtilizationVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyUtilizationVarianceStore(): LegacyUtilizationVarianceStore {
    return MemoryLegacyUtilizationVarianceStore(
        mutableMapOf<String, LegacyUtilizationVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyUtilizationVarianceStore(
    var inner: LegacyUtilizationVarianceStore,
    var cache: TtlCache<LegacyUtilizationVariance>,
    var hits: Int,
    var misses: Int,
) : LegacyUtilizationVarianceStore {
    override fun find(code: String): LegacyUtilizationVariance? {
        val hit = (cache.get(code) as LegacyUtilizationVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyUtilizationVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyUtilizationVariance> = inner.list()
}

fun newCachedLegacyUtilizationVarianceStore(inner: LegacyUtilizationVarianceStore, ttl: Int, clock: Clock): LegacyUtilizationVarianceStore {
    return CachedLegacyUtilizationVarianceStore(
        inner = inner,
        cache = newTtlCache<LegacyUtilizationVariance>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy utilization variances. Empty fields match all. */
data class LegacyUtilizationVarianceQuery(
    var depot: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyUtilizationVariance): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyUtilizationVariance>): MutableList<LegacyUtilizationVariance> {
        val matched = filter(items) { candidate: LegacyUtilizationVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyUtilizationVariances(store: LegacyUtilizationVarianceStore, query: LegacyUtilizationVarianceQuery): MutableList<LegacyUtilizationVariance> {
    return query.apply(store.list())
}
