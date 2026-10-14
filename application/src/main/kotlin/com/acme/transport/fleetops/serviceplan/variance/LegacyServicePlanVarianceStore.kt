package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy service plan variances by code. */
interface LegacyServicePlanVarianceStore {
    fun find(code: String): LegacyServicePlanVariance?
    fun save(item: LegacyServicePlanVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyServicePlanVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyServicePlanVarianceStore(
    var items: MutableMap<String, LegacyServicePlanVariance>,
    var order: MutableList<String>,
) : LegacyServicePlanVarianceStore {
    override fun find(code: String): LegacyServicePlanVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyServicePlanVariance) {
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

    override fun list(): MutableList<LegacyServicePlanVariance> {
        val out = mutableListOf<LegacyServicePlanVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyServicePlanVarianceStore(): LegacyServicePlanVarianceStore {
    return MemoryLegacyServicePlanVarianceStore(
        mutableMapOf<String, LegacyServicePlanVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyServicePlanVarianceStore(
    var inner: LegacyServicePlanVarianceStore,
    var cache: TtlCache<LegacyServicePlanVariance>,
    var hits: Int,
    var misses: Int,
) : LegacyServicePlanVarianceStore {
    override fun find(code: String): LegacyServicePlanVariance? {
        val hit = (cache.get(code) as LegacyServicePlanVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyServicePlanVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyServicePlanVariance> = inner.list()
}

fun newCachedLegacyServicePlanVarianceStore(inner: LegacyServicePlanVarianceStore, ttl: Int, clock: Clock): LegacyServicePlanVarianceStore {
    return CachedLegacyServicePlanVarianceStore(
        inner = inner,
        cache = newTtlCache<LegacyServicePlanVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy service plan variances. Empty fields match all. */
data class LegacyServicePlanVarianceQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyServicePlanVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyServicePlanVariance>): MutableList<LegacyServicePlanVariance> {
        val matched = filter(items) { candidate: LegacyServicePlanVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyServicePlanVariances(store: LegacyServicePlanVarianceStore, query: LegacyServicePlanVarianceQuery): MutableList<LegacyServicePlanVariance> {
    return query.apply(store.list())
}
