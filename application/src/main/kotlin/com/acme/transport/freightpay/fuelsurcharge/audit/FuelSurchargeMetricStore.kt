package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge metrics by code. */
interface FuelSurchargeMetricStore {
    fun find(code: String): FuelSurchargeMetric?
    fun save(item: FuelSurchargeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeMetricStore(
    var items: MutableMap<String, FuelSurchargeMetric>,
    var order: MutableList<String>,
) : FuelSurchargeMetricStore {
    override fun find(code: String): FuelSurchargeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeMetric) {
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

    override fun list(): MutableList<FuelSurchargeMetric> {
        val out = mutableListOf<FuelSurchargeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeMetricStore(): FuelSurchargeMetricStore {
    return MemoryFuelSurchargeMetricStore(mutableMapOf<String, FuelSurchargeMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeMetricStore(
    var inner: FuelSurchargeMetricStore,
    var cache: TtlCache<FuelSurchargeMetric>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeMetricStore {
    override fun find(code: String): FuelSurchargeMetric? {
        val hit = (cache.get(code) as FuelSurchargeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeMetric> = inner.list()
}

fun newCachedFuelSurchargeMetricStore(inner: FuelSurchargeMetricStore, ttl: Int, clock: Clock): FuelSurchargeMetricStore {
    return CachedFuelSurchargeMetricStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge metrics. Empty fields match all. */
data class FuelSurchargeMetricQuery(
    var depot: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeMetric): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeMetric>): MutableList<FuelSurchargeMetric> {
        val matched = filter(items) { candidate: FuelSurchargeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeMetrics(store: FuelSurchargeMetricStore, query: FuelSurchargeMetricQuery): MutableList<FuelSurchargeMetric> {
    return query.apply(store.list())
}
