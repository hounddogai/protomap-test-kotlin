package com.acme.transport.fleetops.defect.defectforecast

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy defect forecasts by code. */
interface LegacyDefectForecastStore {
    fun find(code: String): LegacyDefectForecast?
    fun save(item: LegacyDefectForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDefectForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDefectForecastStore(
    var items: MutableMap<String, LegacyDefectForecast>,
    var order: MutableList<String>,
) : LegacyDefectForecastStore {
    override fun find(code: String): LegacyDefectForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDefectForecast) {
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

    override fun list(): MutableList<LegacyDefectForecast> {
        val out = mutableListOf<LegacyDefectForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDefectForecastStore(): LegacyDefectForecastStore {
    return MemoryLegacyDefectForecastStore(
        mutableMapOf<String, LegacyDefectForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDefectForecastStore(
    var inner: LegacyDefectForecastStore,
    var cache: TtlCache<LegacyDefectForecast>,
    var hits: Int,
    var misses: Int,
) : LegacyDefectForecastStore {
    override fun find(code: String): LegacyDefectForecast? {
        val hit = (cache.get(code) as LegacyDefectForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDefectForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDefectForecast> = inner.list()
}

fun newCachedLegacyDefectForecastStore(inner: LegacyDefectForecastStore, ttl: Int, clock: Clock): LegacyDefectForecastStore {
    return CachedLegacyDefectForecastStore(
        inner = inner,
        cache = newTtlCache<LegacyDefectForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy defect forecasts. Empty fields match all. */
data class LegacyDefectForecastQuery(
    var lane: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDefectForecast): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDefectForecast>): MutableList<LegacyDefectForecast> {
        val matched = filter(items) { candidate: LegacyDefectForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDefectForecasts(store: LegacyDefectForecastStore, query: LegacyDefectForecastQuery): MutableList<LegacyDefectForecast> {
    return query.apply(store.list())
}
