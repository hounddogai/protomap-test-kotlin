package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul forecasts by code. */
interface BackhaulForecastStore {
    fun find(code: String): BackhaulForecast?
    fun save(item: BackhaulForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulForecastStore(
    var items: MutableMap<String, BackhaulForecast>,
    var order: MutableList<String>,
) : BackhaulForecastStore {
    override fun find(code: String): BackhaulForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulForecast) {
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

    override fun list(): MutableList<BackhaulForecast> {
        val out = mutableListOf<BackhaulForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulForecastStore(): BackhaulForecastStore {
    return MemoryBackhaulForecastStore(mutableMapOf<String, BackhaulForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulForecastStore(
    var inner: BackhaulForecastStore,
    var cache: TtlCache<BackhaulForecast>,
    var hits: Int,
    var misses: Int,
) : BackhaulForecastStore {
    override fun find(code: String): BackhaulForecast? {
        val hit = (cache.get(code) as BackhaulForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulForecast> = inner.list()
}

fun newCachedBackhaulForecastStore(inner: BackhaulForecastStore, ttl: Int, clock: Clock): BackhaulForecastStore {
    return CachedBackhaulForecastStore(
        inner = inner,
        cache = newTtlCache<BackhaulForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul forecasts. Empty fields match all. */
data class BackhaulForecastQuery(
    var segment: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulForecast): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<BackhaulForecast>): MutableList<BackhaulForecast> {
        val matched = filter(items) { candidate: BackhaulForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulForecasts(store: BackhaulForecastStore, query: BackhaulForecastQuery): MutableList<BackhaulForecast> {
    return query.apply(store.list())
}
