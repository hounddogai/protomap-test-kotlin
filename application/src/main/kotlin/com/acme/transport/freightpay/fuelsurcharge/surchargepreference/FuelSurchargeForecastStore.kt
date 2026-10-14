package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge forecasts by code. */
interface FuelSurchargeForecastStore {
    fun find(code: String): FuelSurchargeForecast?
    fun save(item: FuelSurchargeForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeForecastStore(
    var items: MutableMap<String, FuelSurchargeForecast>,
    var order: MutableList<String>,
) : FuelSurchargeForecastStore {
    override fun find(code: String): FuelSurchargeForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeForecast) {
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

    override fun list(): MutableList<FuelSurchargeForecast> {
        val out = mutableListOf<FuelSurchargeForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeForecastStore(): FuelSurchargeForecastStore {
    return MemoryFuelSurchargeForecastStore(
        mutableMapOf<String, FuelSurchargeForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeForecastStore(
    var inner: FuelSurchargeForecastStore,
    var cache: TtlCache<FuelSurchargeForecast>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeForecastStore {
    override fun find(code: String): FuelSurchargeForecast? {
        val hit = (cache.get(code) as FuelSurchargeForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeForecast> = inner.list()
}

fun newCachedFuelSurchargeForecastStore(inner: FuelSurchargeForecastStore, ttl: Int, clock: Clock): FuelSurchargeForecastStore {
    return CachedFuelSurchargeForecastStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge forecasts. Empty fields match all. */
data class FuelSurchargeForecastQuery(
    var depot: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeForecast): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeForecast>): MutableList<FuelSurchargeForecast> {
        val matched = filter(items) { candidate: FuelSurchargeForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeForecasts(store: FuelSurchargeForecastStore, query: FuelSurchargeForecastQuery): MutableList<FuelSurchargeForecast> {
    return query.apply(store.list())
}
