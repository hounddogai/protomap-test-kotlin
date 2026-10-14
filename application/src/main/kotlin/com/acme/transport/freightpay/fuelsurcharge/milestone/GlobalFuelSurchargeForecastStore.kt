package com.acme.transport.freightpay.fuelsurcharge.milestone

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global fuel surcharge forecasts by code. */
interface GlobalFuelSurchargeForecastStore {
    fun find(code: String): GlobalFuelSurchargeForecast?
    fun save(item: GlobalFuelSurchargeForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalFuelSurchargeForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalFuelSurchargeForecastStore(
    var items: MutableMap<String, GlobalFuelSurchargeForecast>,
    var order: MutableList<String>,
) : GlobalFuelSurchargeForecastStore {
    override fun find(code: String): GlobalFuelSurchargeForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalFuelSurchargeForecast) {
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

    override fun list(): MutableList<GlobalFuelSurchargeForecast> {
        val out = mutableListOf<GlobalFuelSurchargeForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalFuelSurchargeForecastStore(): GlobalFuelSurchargeForecastStore {
    return MemoryGlobalFuelSurchargeForecastStore(
        mutableMapOf<String, GlobalFuelSurchargeForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalFuelSurchargeForecastStore(
    var inner: GlobalFuelSurchargeForecastStore,
    var cache: TtlCache<GlobalFuelSurchargeForecast>,
    var hits: Int,
    var misses: Int,
) : GlobalFuelSurchargeForecastStore {
    override fun find(code: String): GlobalFuelSurchargeForecast? {
        val hit = (cache.get(code) as GlobalFuelSurchargeForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalFuelSurchargeForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalFuelSurchargeForecast> = inner.list()
}

fun newCachedGlobalFuelSurchargeForecastStore(inner: GlobalFuelSurchargeForecastStore, ttl: Int, clock: Clock): GlobalFuelSurchargeForecastStore {
    return CachedGlobalFuelSurchargeForecastStore(
        inner = inner,
        cache = newTtlCache<GlobalFuelSurchargeForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global fuel surcharge forecasts. Empty fields match all. */
data class GlobalFuelSurchargeForecastQuery(
    var source: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalFuelSurchargeForecast): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalFuelSurchargeForecast>): MutableList<GlobalFuelSurchargeForecast> {
        val matched = filter(items) { candidate: GlobalFuelSurchargeForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalFuelSurchargeForecasts(store: GlobalFuelSurchargeForecastStore, query: GlobalFuelSurchargeForecastQuery): MutableList<GlobalFuelSurchargeForecast> {
    return query.apply(store.list())
}
