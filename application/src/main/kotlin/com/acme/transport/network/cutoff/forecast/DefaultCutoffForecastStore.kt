package com.acme.transport.network.cutoff.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default cutoff forecasts by code. */
interface DefaultCutoffForecastStore {
    fun find(code: String): DefaultCutoffForecast?
    fun save(item: DefaultCutoffForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCutoffForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCutoffForecastStore(
    var items: MutableMap<String, DefaultCutoffForecast>,
    var order: MutableList<String>,
) : DefaultCutoffForecastStore {
    override fun find(code: String): DefaultCutoffForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCutoffForecast) {
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

    override fun list(): MutableList<DefaultCutoffForecast> {
        val out = mutableListOf<DefaultCutoffForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCutoffForecastStore(): DefaultCutoffForecastStore {
    return MemoryDefaultCutoffForecastStore(
        mutableMapOf<String, DefaultCutoffForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCutoffForecastStore(
    var inner: DefaultCutoffForecastStore,
    var cache: TtlCache<DefaultCutoffForecast>,
    var hits: Int,
    var misses: Int,
) : DefaultCutoffForecastStore {
    override fun find(code: String): DefaultCutoffForecast? {
        val hit = (cache.get(code) as DefaultCutoffForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCutoffForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCutoffForecast> = inner.list()
}

fun newCachedDefaultCutoffForecastStore(inner: DefaultCutoffForecastStore, ttl: Int, clock: Clock): DefaultCutoffForecastStore {
    return CachedDefaultCutoffForecastStore(
        inner = inner,
        cache = newTtlCache<DefaultCutoffForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default cutoff forecasts. Empty fields match all. */
data class DefaultCutoffForecastQuery(
    var category: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCutoffForecast): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultCutoffForecast>): MutableList<DefaultCutoffForecast> {
        val matched = filter(items) { candidate: DefaultCutoffForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCutoffForecasts(store: DefaultCutoffForecastStore, query: DefaultCutoffForecastQuery): MutableList<DefaultCutoffForecast> {
    return query.apply(store.list())
}
