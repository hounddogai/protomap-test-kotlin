package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps pending backhaul forecasts by code. */
interface PendingBackhaulForecastStore {
    fun find(code: String): PendingBackhaulForecast?
    fun save(item: PendingBackhaulForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingBackhaulForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingBackhaulForecastStore(
    var items: MutableMap<String, PendingBackhaulForecast>,
    var order: MutableList<String>,
) : PendingBackhaulForecastStore {
    override fun find(code: String): PendingBackhaulForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingBackhaulForecast) {
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

    override fun list(): MutableList<PendingBackhaulForecast> {
        val out = mutableListOf<PendingBackhaulForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingBackhaulForecastStore(): PendingBackhaulForecastStore {
    return MemoryPendingBackhaulForecastStore(
        mutableMapOf<String, PendingBackhaulForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingBackhaulForecastStore(
    var inner: PendingBackhaulForecastStore,
    var cache: TtlCache<PendingBackhaulForecast>,
    var hits: Int,
    var misses: Int,
) : PendingBackhaulForecastStore {
    override fun find(code: String): PendingBackhaulForecast? {
        val hit = (cache.get(code) as PendingBackhaulForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingBackhaulForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingBackhaulForecast> = inner.list()
}

fun newCachedPendingBackhaulForecastStore(inner: PendingBackhaulForecastStore, ttl: Int, clock: Clock): PendingBackhaulForecastStore {
    return CachedPendingBackhaulForecastStore(
        inner = inner,
        cache = newTtlCache<PendingBackhaulForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending backhaul forecasts. Empty fields match all. */
data class PendingBackhaulForecastQuery(
    var region: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingBackhaulForecast): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingBackhaulForecast>): MutableList<PendingBackhaulForecast> {
        val matched = filter(items) { candidate: PendingBackhaulForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingBackhaulForecasts(store: PendingBackhaulForecastStore, query: PendingBackhaulForecastQuery): MutableList<PendingBackhaulForecast> {
    return query.apply(store.list())
}
