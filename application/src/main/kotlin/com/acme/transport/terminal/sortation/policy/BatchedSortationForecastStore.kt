package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched sortation forecasts by code. */
interface BatchedSortationForecastStore {
    fun find(code: String): BatchedSortationForecast?
    fun save(item: BatchedSortationForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedSortationForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedSortationForecastStore(
    var items: MutableMap<String, BatchedSortationForecast>,
    var order: MutableList<String>,
) : BatchedSortationForecastStore {
    override fun find(code: String): BatchedSortationForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedSortationForecast) {
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

    override fun list(): MutableList<BatchedSortationForecast> {
        val out = mutableListOf<BatchedSortationForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedSortationForecastStore(): BatchedSortationForecastStore {
    return MemoryBatchedSortationForecastStore(
        mutableMapOf<String, BatchedSortationForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedSortationForecastStore(
    var inner: BatchedSortationForecastStore,
    var cache: TtlCache<BatchedSortationForecast>,
    var hits: Int,
    var misses: Int,
) : BatchedSortationForecastStore {
    override fun find(code: String): BatchedSortationForecast? {
        val hit = (cache.get(code) as BatchedSortationForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedSortationForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedSortationForecast> = inner.list()
}

fun newCachedBatchedSortationForecastStore(inner: BatchedSortationForecastStore, ttl: Int, clock: Clock): BatchedSortationForecastStore {
    return CachedBatchedSortationForecastStore(
        inner = inner,
        cache = newTtlCache<BatchedSortationForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched sortation forecasts. Empty fields match all. */
data class BatchedSortationForecastQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedSortationForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedSortationForecast>): MutableList<BatchedSortationForecast> {
        val matched = filter(items) { candidate: BatchedSortationForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedSortationForecasts(store: BatchedSortationForecastStore, query: BatchedSortationForecastQuery): MutableList<BatchedSortationForecast> {
    return query.apply(store.list())
}
