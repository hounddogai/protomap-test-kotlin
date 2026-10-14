package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched duty period forecasts by code. */
interface BatchedDutyPeriodForecastStore {
    fun find(code: String): BatchedDutyPeriodForecast?
    fun save(item: BatchedDutyPeriodForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDutyPeriodForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDutyPeriodForecastStore(
    var items: MutableMap<String, BatchedDutyPeriodForecast>,
    var order: MutableList<String>,
) : BatchedDutyPeriodForecastStore {
    override fun find(code: String): BatchedDutyPeriodForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDutyPeriodForecast) {
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

    override fun list(): MutableList<BatchedDutyPeriodForecast> {
        val out = mutableListOf<BatchedDutyPeriodForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDutyPeriodForecastStore(): BatchedDutyPeriodForecastStore {
    return MemoryBatchedDutyPeriodForecastStore(
        mutableMapOf<String, BatchedDutyPeriodForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDutyPeriodForecastStore(
    var inner: BatchedDutyPeriodForecastStore,
    var cache: TtlCache<BatchedDutyPeriodForecast>,
    var hits: Int,
    var misses: Int,
) : BatchedDutyPeriodForecastStore {
    override fun find(code: String): BatchedDutyPeriodForecast? {
        val hit = (cache.get(code) as BatchedDutyPeriodForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDutyPeriodForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDutyPeriodForecast> = inner.list()
}

fun newCachedBatchedDutyPeriodForecastStore(inner: BatchedDutyPeriodForecastStore, ttl: Int, clock: Clock): BatchedDutyPeriodForecastStore {
    return CachedBatchedDutyPeriodForecastStore(
        inner = inner,
        cache = newTtlCache<BatchedDutyPeriodForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched duty period forecasts. Empty fields match all. */
data class BatchedDutyPeriodForecastQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDutyPeriodForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDutyPeriodForecast>): MutableList<BatchedDutyPeriodForecast> {
        val matched = filter(items) { candidate: BatchedDutyPeriodForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDutyPeriodForecasts(store: BatchedDutyPeriodForecastStore, query: BatchedDutyPeriodForecastQuery): MutableList<BatchedDutyPeriodForecast> {
    return query.apply(store.list())
}
