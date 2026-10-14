package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched parcel forecasts by code. */
interface BatchedParcelForecastStore {
    fun find(code: String): BatchedParcelForecast?
    fun save(item: BatchedParcelForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedParcelForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedParcelForecastStore(
    var items: MutableMap<String, BatchedParcelForecast>,
    var order: MutableList<String>,
) : BatchedParcelForecastStore {
    override fun find(code: String): BatchedParcelForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedParcelForecast) {
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

    override fun list(): MutableList<BatchedParcelForecast> {
        val out = mutableListOf<BatchedParcelForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedParcelForecastStore(): BatchedParcelForecastStore {
    return MemoryBatchedParcelForecastStore(
        mutableMapOf<String, BatchedParcelForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedParcelForecastStore(
    var inner: BatchedParcelForecastStore,
    var cache: TtlCache<BatchedParcelForecast>,
    var hits: Int,
    var misses: Int,
) : BatchedParcelForecastStore {
    override fun find(code: String): BatchedParcelForecast? {
        val hit = (cache.get(code) as BatchedParcelForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedParcelForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedParcelForecast> = inner.list()
}

fun newCachedBatchedParcelForecastStore(inner: BatchedParcelForecastStore, ttl: Int, clock: Clock): BatchedParcelForecastStore {
    return CachedBatchedParcelForecastStore(
        inner = inner,
        cache = newTtlCache<BatchedParcelForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched parcel forecasts. Empty fields match all. */
data class BatchedParcelForecastQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedParcelForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<BatchedParcelForecast>): MutableList<BatchedParcelForecast> {
        val matched = filter(items) { candidate: BatchedParcelForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedParcelForecasts(store: BatchedParcelForecastStore, query: BatchedParcelForecastQuery): MutableList<BatchedParcelForecast> {
    return query.apply(store.list())
}
