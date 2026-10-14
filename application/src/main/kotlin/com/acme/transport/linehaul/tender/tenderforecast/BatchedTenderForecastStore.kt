package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched tender forecasts by code. */
interface BatchedTenderForecastStore {
    fun find(code: String): BatchedTenderForecast?
    fun save(item: BatchedTenderForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTenderForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTenderForecastStore(
    var items: MutableMap<String, BatchedTenderForecast>,
    var order: MutableList<String>,
) : BatchedTenderForecastStore {
    override fun find(code: String): BatchedTenderForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTenderForecast) {
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

    override fun list(): MutableList<BatchedTenderForecast> {
        val out = mutableListOf<BatchedTenderForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTenderForecastStore(): BatchedTenderForecastStore {
    return MemoryBatchedTenderForecastStore(
        mutableMapOf<String, BatchedTenderForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTenderForecastStore(
    var inner: BatchedTenderForecastStore,
    var cache: TtlCache<BatchedTenderForecast>,
    var hits: Int,
    var misses: Int,
) : BatchedTenderForecastStore {
    override fun find(code: String): BatchedTenderForecast? {
        val hit = (cache.get(code) as BatchedTenderForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTenderForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTenderForecast> = inner.list()
}

fun newCachedBatchedTenderForecastStore(inner: BatchedTenderForecastStore, ttl: Int, clock: Clock): BatchedTenderForecastStore {
    return CachedBatchedTenderForecastStore(
        inner = inner,
        cache = newTtlCache<BatchedTenderForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched tender forecasts. Empty fields match all. */
data class BatchedTenderForecastQuery(
    var label: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTenderForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<BatchedTenderForecast>): MutableList<BatchedTenderForecast> {
        val matched = filter(items) { candidate: BatchedTenderForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTenderForecasts(store: BatchedTenderForecastStore, query: BatchedTenderForecastQuery): MutableList<BatchedTenderForecast> {
    return query.apply(store.list())
}
