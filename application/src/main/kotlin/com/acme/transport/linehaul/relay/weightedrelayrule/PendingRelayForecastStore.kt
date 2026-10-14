package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending relay forecasts by code. */
interface PendingRelayForecastStore {
    fun find(code: String): PendingRelayForecast?
    fun save(item: PendingRelayForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRelayForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRelayForecastStore(
    var items: MutableMap<String, PendingRelayForecast>,
    var order: MutableList<String>,
) : PendingRelayForecastStore {
    override fun find(code: String): PendingRelayForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRelayForecast) {
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

    override fun list(): MutableList<PendingRelayForecast> {
        val out = mutableListOf<PendingRelayForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRelayForecastStore(): PendingRelayForecastStore {
    return MemoryPendingRelayForecastStore(
        mutableMapOf<String, PendingRelayForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRelayForecastStore(
    var inner: PendingRelayForecastStore,
    var cache: TtlCache<PendingRelayForecast>,
    var hits: Int,
    var misses: Int,
) : PendingRelayForecastStore {
    override fun find(code: String): PendingRelayForecast? {
        val hit = (cache.get(code) as PendingRelayForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRelayForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRelayForecast> = inner.list()
}

fun newCachedPendingRelayForecastStore(inner: PendingRelayForecastStore, ttl: Int, clock: Clock): PendingRelayForecastStore {
    return CachedPendingRelayForecastStore(
        inner = inner,
        cache = newTtlCache<PendingRelayForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending relay forecasts. Empty fields match all. */
data class PendingRelayForecastQuery(
    var country: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRelayForecast): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRelayForecast>): MutableList<PendingRelayForecast> {
        val matched = filter(items) { candidate: PendingRelayForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRelayForecasts(store: PendingRelayForecastStore, query: PendingRelayForecastQuery): MutableList<PendingRelayForecast> {
    return query.apply(store.list())
}
