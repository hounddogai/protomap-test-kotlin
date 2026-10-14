package com.acme.transport.safety.cameraevent.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps pending camera event forecasts by code. */
interface PendingCameraEventForecastStore {
    fun find(code: String): PendingCameraEventForecast?
    fun save(item: PendingCameraEventForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCameraEventForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCameraEventForecastStore(
    var items: MutableMap<String, PendingCameraEventForecast>,
    var order: MutableList<String>,
) : PendingCameraEventForecastStore {
    override fun find(code: String): PendingCameraEventForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCameraEventForecast) {
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

    override fun list(): MutableList<PendingCameraEventForecast> {
        val out = mutableListOf<PendingCameraEventForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCameraEventForecastStore(): PendingCameraEventForecastStore {
    return MemoryPendingCameraEventForecastStore(
        mutableMapOf<String, PendingCameraEventForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCameraEventForecastStore(
    var inner: PendingCameraEventForecastStore,
    var cache: TtlCache<PendingCameraEventForecast>,
    var hits: Int,
    var misses: Int,
) : PendingCameraEventForecastStore {
    override fun find(code: String): PendingCameraEventForecast? {
        val hit = (cache.get(code) as PendingCameraEventForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCameraEventForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCameraEventForecast> = inner.list()
}

fun newCachedPendingCameraEventForecastStore(inner: PendingCameraEventForecastStore, ttl: Int, clock: Clock): PendingCameraEventForecastStore {
    return CachedPendingCameraEventForecastStore(
        inner = inner,
        cache = newTtlCache<PendingCameraEventForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending camera event forecasts. Empty fields match all. */
data class PendingCameraEventForecastQuery(
    var unit: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCameraEventForecast): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<PendingCameraEventForecast>): MutableList<PendingCameraEventForecast> {
        val matched = filter(items) { candidate: PendingCameraEventForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCameraEventForecasts(store: PendingCameraEventForecastStore, query: PendingCameraEventForecastQuery): MutableList<PendingCameraEventForecast> {
    return query.apply(store.list())
}
