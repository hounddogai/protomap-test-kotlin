package com.acme.transport.freightpay.accessorial.scheduledaccessorialcycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual accessorial forecasts by code. */
interface ManualAccessorialForecastStore {
    fun find(code: String): ManualAccessorialForecast?
    fun save(item: ManualAccessorialForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualAccessorialForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualAccessorialForecastStore(
    var items: MutableMap<String, ManualAccessorialForecast>,
    var order: MutableList<String>,
) : ManualAccessorialForecastStore {
    override fun find(code: String): ManualAccessorialForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualAccessorialForecast) {
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

    override fun list(): MutableList<ManualAccessorialForecast> {
        val out = mutableListOf<ManualAccessorialForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualAccessorialForecastStore(): ManualAccessorialForecastStore {
    return MemoryManualAccessorialForecastStore(
        mutableMapOf<String, ManualAccessorialForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualAccessorialForecastStore(
    var inner: ManualAccessorialForecastStore,
    var cache: TtlCache<ManualAccessorialForecast>,
    var hits: Int,
    var misses: Int,
) : ManualAccessorialForecastStore {
    override fun find(code: String): ManualAccessorialForecast? {
        val hit = (cache.get(code) as ManualAccessorialForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualAccessorialForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualAccessorialForecast> = inner.list()
}

fun newCachedManualAccessorialForecastStore(inner: ManualAccessorialForecastStore, ttl: Int, clock: Clock): ManualAccessorialForecastStore {
    return CachedManualAccessorialForecastStore(
        inner = inner,
        cache = newTtlCache<ManualAccessorialForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual accessorial forecasts. Empty fields match all. */
data class ManualAccessorialForecastQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualAccessorialForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualAccessorialForecast>): MutableList<ManualAccessorialForecast> {
        val matched = filter(items) { candidate: ManualAccessorialForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualAccessorialForecasts(store: ManualAccessorialForecastStore, query: ManualAccessorialForecastQuery): MutableList<ManualAccessorialForecast> {
    return query.apply(store.list())
}
