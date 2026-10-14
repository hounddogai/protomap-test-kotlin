package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched lease return forecasts by code. */
interface BatchedLeaseReturnForecastStore {
    fun find(code: String): BatchedLeaseReturnForecast?
    fun save(item: BatchedLeaseReturnForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedLeaseReturnForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedLeaseReturnForecastStore(
    var items: MutableMap<String, BatchedLeaseReturnForecast>,
    var order: MutableList<String>,
) : BatchedLeaseReturnForecastStore {
    override fun find(code: String): BatchedLeaseReturnForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedLeaseReturnForecast) {
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

    override fun list(): MutableList<BatchedLeaseReturnForecast> {
        val out = mutableListOf<BatchedLeaseReturnForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedLeaseReturnForecastStore(): BatchedLeaseReturnForecastStore {
    return MemoryBatchedLeaseReturnForecastStore(
        mutableMapOf<String, BatchedLeaseReturnForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedLeaseReturnForecastStore(
    var inner: BatchedLeaseReturnForecastStore,
    var cache: TtlCache<BatchedLeaseReturnForecast>,
    var hits: Int,
    var misses: Int,
) : BatchedLeaseReturnForecastStore {
    override fun find(code: String): BatchedLeaseReturnForecast? {
        val hit = (cache.get(code) as BatchedLeaseReturnForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedLeaseReturnForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedLeaseReturnForecast> = inner.list()
}

fun newCachedBatchedLeaseReturnForecastStore(inner: BatchedLeaseReturnForecastStore, ttl: Int, clock: Clock): BatchedLeaseReturnForecastStore {
    return CachedBatchedLeaseReturnForecastStore(
        inner = inner,
        cache = newTtlCache<BatchedLeaseReturnForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched lease return forecasts. Empty fields match all. */
data class BatchedLeaseReturnForecastQuery(
    var region: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedLeaseReturnForecast): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedLeaseReturnForecast>): MutableList<BatchedLeaseReturnForecast> {
        val matched = filter(items) { candidate: BatchedLeaseReturnForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedLeaseReturnForecasts(store: BatchedLeaseReturnForecastStore, query: BatchedLeaseReturnForecastQuery): MutableList<BatchedLeaseReturnForecast> {
    return query.apply(store.list())
}
