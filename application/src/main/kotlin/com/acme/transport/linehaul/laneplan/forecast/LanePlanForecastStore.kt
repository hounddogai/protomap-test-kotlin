package com.acme.transport.linehaul.laneplan.forecast

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lane plan forecasts by code. */
interface LanePlanForecastStore {
    fun find(code: String): LanePlanForecast?
    fun save(item: LanePlanForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<LanePlanForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLanePlanForecastStore(
    var items: MutableMap<String, LanePlanForecast>,
    var order: MutableList<String>,
) : LanePlanForecastStore {
    override fun find(code: String): LanePlanForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LanePlanForecast) {
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

    override fun list(): MutableList<LanePlanForecast> {
        val out = mutableListOf<LanePlanForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLanePlanForecastStore(): LanePlanForecastStore {
    return MemoryLanePlanForecastStore(mutableMapOf<String, LanePlanForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLanePlanForecastStore(
    var inner: LanePlanForecastStore,
    var cache: TtlCache<LanePlanForecast>,
    var hits: Int,
    var misses: Int,
) : LanePlanForecastStore {
    override fun find(code: String): LanePlanForecast? {
        val hit = (cache.get(code) as LanePlanForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LanePlanForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LanePlanForecast> = inner.list()
}

fun newCachedLanePlanForecastStore(inner: LanePlanForecastStore, ttl: Int, clock: Clock): LanePlanForecastStore {
    return CachedLanePlanForecastStore(
        inner = inner,
        cache = newTtlCache<LanePlanForecast>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lane plan forecasts. Empty fields match all. */
data class LanePlanForecastQuery(
    var category: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LanePlanForecast): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LanePlanForecast>): MutableList<LanePlanForecast> {
        val matched = filter(items) { candidate: LanePlanForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLanePlanForecasts(store: LanePlanForecastStore, query: LanePlanForecastQuery): MutableList<LanePlanForecast> {
    return query.apply(store.list())
}
