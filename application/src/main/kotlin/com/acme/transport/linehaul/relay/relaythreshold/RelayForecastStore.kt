package com.acme.transport.linehaul.relay.relaythreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay forecasts by code. */
interface RelayForecastStore {
    fun find(code: String): RelayForecast?
    fun save(item: RelayForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayForecastStore(
    var items: MutableMap<String, RelayForecast>,
    var order: MutableList<String>,
) : RelayForecastStore {
    override fun find(code: String): RelayForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayForecast) {
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

    override fun list(): MutableList<RelayForecast> {
        val out = mutableListOf<RelayForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayForecastStore(): RelayForecastStore {
    return MemoryRelayForecastStore(mutableMapOf<String, RelayForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayForecastStore(
    var inner: RelayForecastStore,
    var cache: TtlCache<RelayForecast>,
    var hits: Int,
    var misses: Int,
) : RelayForecastStore {
    override fun find(code: String): RelayForecast? {
        val hit = (cache.get(code) as RelayForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayForecast> = inner.list()
}

fun newCachedRelayForecastStore(inner: RelayForecastStore, ttl: Int, clock: Clock): RelayForecastStore {
    return CachedRelayForecastStore(
        inner = inner,
        cache = newTtlCache<RelayForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay forecasts. Empty fields match all. */
data class RelayForecastQuery(
    var lane: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayForecast): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<RelayForecast>): MutableList<RelayForecast> {
        val matched = filter(items) { candidate: RelayForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayForecasts(store: RelayForecastStore, query: RelayForecastQuery): MutableList<RelayForecast> {
    return query.apply(store.list())
}
