package com.acme.transport.lastmile.redelivery.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps strict redelivery forecasts by code. */
interface StrictRedeliveryForecastStore {
    fun find(code: String): StrictRedeliveryForecast?
    fun save(item: StrictRedeliveryForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRedeliveryForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRedeliveryForecastStore(
    var items: MutableMap<String, StrictRedeliveryForecast>,
    var order: MutableList<String>,
) : StrictRedeliveryForecastStore {
    override fun find(code: String): StrictRedeliveryForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRedeliveryForecast) {
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

    override fun list(): MutableList<StrictRedeliveryForecast> {
        val out = mutableListOf<StrictRedeliveryForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRedeliveryForecastStore(): StrictRedeliveryForecastStore {
    return MemoryStrictRedeliveryForecastStore(
        mutableMapOf<String, StrictRedeliveryForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRedeliveryForecastStore(
    var inner: StrictRedeliveryForecastStore,
    var cache: TtlCache<StrictRedeliveryForecast>,
    var hits: Int,
    var misses: Int,
) : StrictRedeliveryForecastStore {
    override fun find(code: String): StrictRedeliveryForecast? {
        val hit = (cache.get(code) as StrictRedeliveryForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRedeliveryForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRedeliveryForecast> = inner.list()
}

fun newCachedStrictRedeliveryForecastStore(inner: StrictRedeliveryForecastStore, ttl: Int, clock: Clock): StrictRedeliveryForecastStore {
    return CachedStrictRedeliveryForecastStore(
        inner = inner,
        cache = newTtlCache<StrictRedeliveryForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict redelivery forecasts. Empty fields match all. */
data class StrictRedeliveryForecastQuery(
    var locale: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRedeliveryForecast): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<StrictRedeliveryForecast>): MutableList<StrictRedeliveryForecast> {
        val matched = filter(items) { candidate: StrictRedeliveryForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRedeliveryForecasts(store: StrictRedeliveryForecastStore, query: StrictRedeliveryForecastQuery): MutableList<StrictRedeliveryForecast> {
    return query.apply(store.list())
}
