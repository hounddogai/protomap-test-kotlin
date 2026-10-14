package com.acme.transport.safety.nearmiss.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss forecasts by code. */
interface NearMissForecastStore {
    fun find(code: String): NearMissForecast?
    fun save(item: NearMissForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissForecastStore(
    var items: MutableMap<String, NearMissForecast>,
    var order: MutableList<String>,
) : NearMissForecastStore {
    override fun find(code: String): NearMissForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissForecast) {
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

    override fun list(): MutableList<NearMissForecast> {
        val out = mutableListOf<NearMissForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissForecastStore(): NearMissForecastStore {
    return MemoryNearMissForecastStore(mutableMapOf<String, NearMissForecast>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissForecastStore(
    var inner: NearMissForecastStore,
    var cache: TtlCache<NearMissForecast>,
    var hits: Int,
    var misses: Int,
) : NearMissForecastStore {
    override fun find(code: String): NearMissForecast? {
        val hit = (cache.get(code) as NearMissForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissForecast> = inner.list()
}

fun newCachedNearMissForecastStore(inner: NearMissForecastStore, ttl: Int, clock: Clock): NearMissForecastStore {
    return CachedNearMissForecastStore(
        inner = inner,
        cache = newTtlCache<NearMissForecast>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss forecasts. Empty fields match all. */
data class NearMissForecastQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissForecast>): MutableList<NearMissForecast> {
        val matched = filter(items) { candidate: NearMissForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissForecasts(store: NearMissForecastStore, query: NearMissForecastQuery): MutableList<NearMissForecast> {
    return query.apply(store.list())
}
