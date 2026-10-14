package com.acme.transport.linehaul.tender.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender forecasts by code. */
interface TenderForecastStore {
    fun find(code: String): TenderForecast?
    fun save(item: TenderForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderForecastStore(
    var items: MutableMap<String, TenderForecast>,
    var order: MutableList<String>,
) : TenderForecastStore {
    override fun find(code: String): TenderForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderForecast) {
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

    override fun list(): MutableList<TenderForecast> {
        val out = mutableListOf<TenderForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderForecastStore(): TenderForecastStore {
    return MemoryTenderForecastStore(mutableMapOf<String, TenderForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderForecastStore(
    var inner: TenderForecastStore,
    var cache: TtlCache<TenderForecast>,
    var hits: Int,
    var misses: Int,
) : TenderForecastStore {
    override fun find(code: String): TenderForecast? {
        val hit = (cache.get(code) as TenderForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderForecast> = inner.list()
}

fun newCachedTenderForecastStore(inner: TenderForecastStore, ttl: Int, clock: Clock): TenderForecastStore {
    return CachedTenderForecastStore(
        inner = inner,
        cache = newTtlCache<TenderForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender forecasts. Empty fields match all. */
data class TenderForecastQuery(
    var category: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderForecast): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderForecast>): MutableList<TenderForecast> {
        val matched = filter(items) { candidate: TenderForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderForecasts(store: TenderForecastStore, query: TenderForecastQuery): MutableList<TenderForecast> {
    return query.apply(store.list())
}
