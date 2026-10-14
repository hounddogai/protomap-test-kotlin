package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event forecasts by code. */
interface CameraEventForecastStore {
    fun find(code: String): CameraEventForecast?
    fun save(item: CameraEventForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventForecastStore(
    var items: MutableMap<String, CameraEventForecast>,
    var order: MutableList<String>,
) : CameraEventForecastStore {
    override fun find(code: String): CameraEventForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventForecast) {
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

    override fun list(): MutableList<CameraEventForecast> {
        val out = mutableListOf<CameraEventForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventForecastStore(): CameraEventForecastStore {
    return MemoryCameraEventForecastStore(mutableMapOf<String, CameraEventForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventForecastStore(
    var inner: CameraEventForecastStore,
    var cache: TtlCache<CameraEventForecast>,
    var hits: Int,
    var misses: Int,
) : CameraEventForecastStore {
    override fun find(code: String): CameraEventForecast? {
        val hit = (cache.get(code) as CameraEventForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventForecast> = inner.list()
}

fun newCachedCameraEventForecastStore(inner: CameraEventForecastStore, ttl: Int, clock: Clock): CameraEventForecastStore {
    return CachedCameraEventForecastStore(
        inner = inner,
        cache = newTtlCache<CameraEventForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event forecasts. Empty fields match all. */
data class CameraEventForecastQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CameraEventForecast>): MutableList<CameraEventForecast> {
        val matched = filter(items) { candidate: CameraEventForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventForecasts(store: CameraEventForecastStore, query: CameraEventForecastQuery): MutableList<CameraEventForecast> {
    return query.apply(store.list())
}
