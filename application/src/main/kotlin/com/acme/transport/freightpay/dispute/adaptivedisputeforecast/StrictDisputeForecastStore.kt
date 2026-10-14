package com.acme.transport.freightpay.dispute.adaptivedisputeforecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict dispute forecasts by code. */
interface StrictDisputeForecastStore {
    fun find(code: String): StrictDisputeForecast?
    fun save(item: StrictDisputeForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDisputeForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDisputeForecastStore(
    var items: MutableMap<String, StrictDisputeForecast>,
    var order: MutableList<String>,
) : StrictDisputeForecastStore {
    override fun find(code: String): StrictDisputeForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDisputeForecast) {
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

    override fun list(): MutableList<StrictDisputeForecast> {
        val out = mutableListOf<StrictDisputeForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDisputeForecastStore(): StrictDisputeForecastStore {
    return MemoryStrictDisputeForecastStore(
        mutableMapOf<String, StrictDisputeForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDisputeForecastStore(
    var inner: StrictDisputeForecastStore,
    var cache: TtlCache<StrictDisputeForecast>,
    var hits: Int,
    var misses: Int,
) : StrictDisputeForecastStore {
    override fun find(code: String): StrictDisputeForecast? {
        val hit = (cache.get(code) as StrictDisputeForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDisputeForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDisputeForecast> = inner.list()
}

fun newCachedStrictDisputeForecastStore(inner: StrictDisputeForecastStore, ttl: Int, clock: Clock): StrictDisputeForecastStore {
    return CachedStrictDisputeForecastStore(
        inner = inner,
        cache = newTtlCache<StrictDisputeForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dispute forecasts. Empty fields match all. */
data class StrictDisputeForecastQuery(
    var channel: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDisputeForecast): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDisputeForecast>): MutableList<StrictDisputeForecast> {
        val matched = filter(items) { candidate: StrictDisputeForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDisputeForecasts(store: StrictDisputeForecastStore, query: StrictDisputeForecastQuery): MutableList<StrictDisputeForecast> {
    return query.apply(store.list())
}
