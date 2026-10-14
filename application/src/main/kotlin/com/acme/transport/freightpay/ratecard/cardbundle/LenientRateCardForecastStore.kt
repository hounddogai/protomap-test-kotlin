package com.acme.transport.freightpay.ratecard.cardbundle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient rate card forecasts by code. */
interface LenientRateCardForecastStore {
    fun find(code: String): LenientRateCardForecast?
    fun save(item: LenientRateCardForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRateCardForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRateCardForecastStore(
    var items: MutableMap<String, LenientRateCardForecast>,
    var order: MutableList<String>,
) : LenientRateCardForecastStore {
    override fun find(code: String): LenientRateCardForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRateCardForecast) {
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

    override fun list(): MutableList<LenientRateCardForecast> {
        val out = mutableListOf<LenientRateCardForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRateCardForecastStore(): LenientRateCardForecastStore {
    return MemoryLenientRateCardForecastStore(
        mutableMapOf<String, LenientRateCardForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRateCardForecastStore(
    var inner: LenientRateCardForecastStore,
    var cache: TtlCache<LenientRateCardForecast>,
    var hits: Int,
    var misses: Int,
) : LenientRateCardForecastStore {
    override fun find(code: String): LenientRateCardForecast? {
        val hit = (cache.get(code) as LenientRateCardForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRateCardForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRateCardForecast> = inner.list()
}

fun newCachedLenientRateCardForecastStore(inner: LenientRateCardForecastStore, ttl: Int, clock: Clock): LenientRateCardForecastStore {
    return CachedLenientRateCardForecastStore(
        inner = inner,
        cache = newTtlCache<LenientRateCardForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient rate card forecasts. Empty fields match all. */
data class LenientRateCardForecastQuery(
    var unit: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRateCardForecast): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRateCardForecast>): MutableList<LenientRateCardForecast> {
        val matched = filter(items) { candidate: LenientRateCardForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRateCardForecasts(store: LenientRateCardForecastStore, query: LenientRateCardForecastQuery): MutableList<LenientRateCardForecast> {
    return query.apply(store.list())
}
