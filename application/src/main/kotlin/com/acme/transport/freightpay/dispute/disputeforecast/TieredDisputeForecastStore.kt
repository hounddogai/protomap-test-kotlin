package com.acme.transport.freightpay.dispute.disputeforecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered dispute forecasts by code. */
interface TieredDisputeForecastStore {
    fun find(code: String): TieredDisputeForecast?
    fun save(item: TieredDisputeForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDisputeForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDisputeForecastStore(
    var items: MutableMap<String, TieredDisputeForecast>,
    var order: MutableList<String>,
) : TieredDisputeForecastStore {
    override fun find(code: String): TieredDisputeForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDisputeForecast) {
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

    override fun list(): MutableList<TieredDisputeForecast> {
        val out = mutableListOf<TieredDisputeForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDisputeForecastStore(): TieredDisputeForecastStore {
    return MemoryTieredDisputeForecastStore(
        mutableMapOf<String, TieredDisputeForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDisputeForecastStore(
    var inner: TieredDisputeForecastStore,
    var cache: TtlCache<TieredDisputeForecast>,
    var hits: Int,
    var misses: Int,
) : TieredDisputeForecastStore {
    override fun find(code: String): TieredDisputeForecast? {
        val hit = (cache.get(code) as TieredDisputeForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDisputeForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDisputeForecast> = inner.list()
}

fun newCachedTieredDisputeForecastStore(inner: TieredDisputeForecastStore, ttl: Int, clock: Clock): TieredDisputeForecastStore {
    return CachedTieredDisputeForecastStore(
        inner = inner,
        cache = newTtlCache<TieredDisputeForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered dispute forecasts. Empty fields match all. */
data class TieredDisputeForecastQuery(
    var channel: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDisputeForecast): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDisputeForecast>): MutableList<TieredDisputeForecast> {
        val matched = filter(items) { candidate: TieredDisputeForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDisputeForecasts(store: TieredDisputeForecastStore, query: TieredDisputeForecastQuery): MutableList<TieredDisputeForecast> {
    return query.apply(store.list())
}
