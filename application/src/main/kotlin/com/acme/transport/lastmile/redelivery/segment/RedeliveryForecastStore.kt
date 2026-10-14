package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps redelivery forecasts by code. */
interface RedeliveryForecastStore {
    fun find(code: String): RedeliveryForecast?
    fun save(item: RedeliveryForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<RedeliveryForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRedeliveryForecastStore(
    var items: MutableMap<String, RedeliveryForecast>,
    var order: MutableList<String>,
) : RedeliveryForecastStore {
    override fun find(code: String): RedeliveryForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RedeliveryForecast) {
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

    override fun list(): MutableList<RedeliveryForecast> {
        val out = mutableListOf<RedeliveryForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRedeliveryForecastStore(): RedeliveryForecastStore {
    return MemoryRedeliveryForecastStore(mutableMapOf<String, RedeliveryForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRedeliveryForecastStore(
    var inner: RedeliveryForecastStore,
    var cache: TtlCache<RedeliveryForecast>,
    var hits: Int,
    var misses: Int,
) : RedeliveryForecastStore {
    override fun find(code: String): RedeliveryForecast? {
        val hit = (cache.get(code) as RedeliveryForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RedeliveryForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RedeliveryForecast> = inner.list()
}

fun newCachedRedeliveryForecastStore(inner: RedeliveryForecastStore, ttl: Int, clock: Clock): RedeliveryForecastStore {
    return CachedRedeliveryForecastStore(
        inner = inner,
        cache = newTtlCache<RedeliveryForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects redelivery forecasts. Empty fields match all. */
data class RedeliveryForecastQuery(
    var country: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RedeliveryForecast): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RedeliveryForecast>): MutableList<RedeliveryForecast> {
        val matched = filter(items) { candidate: RedeliveryForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRedeliveryForecasts(store: RedeliveryForecastStore, query: RedeliveryForecastQuery): MutableList<RedeliveryForecast> {
    return query.apply(store.list())
}
