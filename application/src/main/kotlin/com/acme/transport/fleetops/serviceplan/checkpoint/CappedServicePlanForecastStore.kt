package com.acme.transport.fleetops.serviceplan.checkpoint

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped service plan forecasts by code. */
interface CappedServicePlanForecastStore {
    fun find(code: String): CappedServicePlanForecast?
    fun save(item: CappedServicePlanForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedServicePlanForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedServicePlanForecastStore(
    var items: MutableMap<String, CappedServicePlanForecast>,
    var order: MutableList<String>,
) : CappedServicePlanForecastStore {
    override fun find(code: String): CappedServicePlanForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedServicePlanForecast) {
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

    override fun list(): MutableList<CappedServicePlanForecast> {
        val out = mutableListOf<CappedServicePlanForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedServicePlanForecastStore(): CappedServicePlanForecastStore {
    return MemoryCappedServicePlanForecastStore(
        mutableMapOf<String, CappedServicePlanForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedServicePlanForecastStore(
    var inner: CappedServicePlanForecastStore,
    var cache: TtlCache<CappedServicePlanForecast>,
    var hits: Int,
    var misses: Int,
) : CappedServicePlanForecastStore {
    override fun find(code: String): CappedServicePlanForecast? {
        val hit = (cache.get(code) as CappedServicePlanForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedServicePlanForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedServicePlanForecast> = inner.list()
}

fun newCachedCappedServicePlanForecastStore(inner: CappedServicePlanForecastStore, ttl: Int, clock: Clock): CappedServicePlanForecastStore {
    return CachedCappedServicePlanForecastStore(
        inner = inner,
        cache = newTtlCache<CappedServicePlanForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped service plan forecasts. Empty fields match all. */
data class CappedServicePlanForecastQuery(
    var title: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedServicePlanForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedServicePlanForecast>): MutableList<CappedServicePlanForecast> {
        val matched = filter(items) { candidate: CappedServicePlanForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedServicePlanForecasts(store: CappedServicePlanForecastStore, query: CappedServicePlanForecastQuery): MutableList<CappedServicePlanForecast> {
    return query.apply(store.list())
}
