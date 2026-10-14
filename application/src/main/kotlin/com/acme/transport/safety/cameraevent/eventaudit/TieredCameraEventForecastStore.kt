package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps tiered camera event forecasts by code. */
interface TieredCameraEventForecastStore {
    fun find(code: String): TieredCameraEventForecast?
    fun save(item: TieredCameraEventForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCameraEventForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCameraEventForecastStore(
    var items: MutableMap<String, TieredCameraEventForecast>,
    var order: MutableList<String>,
) : TieredCameraEventForecastStore {
    override fun find(code: String): TieredCameraEventForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCameraEventForecast) {
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

    override fun list(): MutableList<TieredCameraEventForecast> {
        val out = mutableListOf<TieredCameraEventForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCameraEventForecastStore(): TieredCameraEventForecastStore {
    return MemoryTieredCameraEventForecastStore(
        mutableMapOf<String, TieredCameraEventForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCameraEventForecastStore(
    var inner: TieredCameraEventForecastStore,
    var cache: TtlCache<TieredCameraEventForecast>,
    var hits: Int,
    var misses: Int,
) : TieredCameraEventForecastStore {
    override fun find(code: String): TieredCameraEventForecast? {
        val hit = (cache.get(code) as TieredCameraEventForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCameraEventForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCameraEventForecast> = inner.list()
}

fun newCachedTieredCameraEventForecastStore(inner: TieredCameraEventForecastStore, ttl: Int, clock: Clock): TieredCameraEventForecastStore {
    return CachedTieredCameraEventForecastStore(
        inner = inner,
        cache = newTtlCache<TieredCameraEventForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered camera event forecasts. Empty fields match all. */
data class TieredCameraEventForecastQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCameraEventForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCameraEventForecast>): MutableList<TieredCameraEventForecast> {
        val matched = filter(items) { candidate: TieredCameraEventForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCameraEventForecasts(store: TieredCameraEventForecastStore, query: TieredCameraEventForecastQuery): MutableList<TieredCameraEventForecast> {
    return query.apply(store.list())
}
