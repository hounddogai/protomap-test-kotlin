package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute forecasts by code. */
interface DisputeForecastStore {
    fun find(code: String): DisputeForecast?
    fun save(item: DisputeForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeForecastStore(
    var items: MutableMap<String, DisputeForecast>,
    var order: MutableList<String>,
) : DisputeForecastStore {
    override fun find(code: String): DisputeForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeForecast) {
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

    override fun list(): MutableList<DisputeForecast> {
        val out = mutableListOf<DisputeForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeForecastStore(): DisputeForecastStore {
    return MemoryDisputeForecastStore(mutableMapOf<String, DisputeForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeForecastStore(
    var inner: DisputeForecastStore,
    var cache: TtlCache<DisputeForecast>,
    var hits: Int,
    var misses: Int,
) : DisputeForecastStore {
    override fun find(code: String): DisputeForecast? {
        val hit = (cache.get(code) as DisputeForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeForecast> = inner.list()
}

fun newCachedDisputeForecastStore(inner: DisputeForecastStore, ttl: Int, clock: Clock): DisputeForecastStore {
    return CachedDisputeForecastStore(
        inner = inner,
        cache = newTtlCache<DisputeForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute forecasts. Empty fields match all. */
data class DisputeForecastQuery(
    var lane: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeForecast): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeForecast>): MutableList<DisputeForecast> {
        val matched = filter(items) { candidate: DisputeForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeForecasts(store: DisputeForecastStore, query: DisputeForecastQuery): MutableList<DisputeForecast> {
    return query.apply(store.list())
}
