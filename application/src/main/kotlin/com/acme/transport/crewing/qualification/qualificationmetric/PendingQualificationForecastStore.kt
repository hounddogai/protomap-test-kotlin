package com.acme.transport.crewing.qualification.qualificationmetric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending qualification forecasts by code. */
interface PendingQualificationForecastStore {
    fun find(code: String): PendingQualificationForecast?
    fun save(item: PendingQualificationForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingQualificationForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingQualificationForecastStore(
    var items: MutableMap<String, PendingQualificationForecast>,
    var order: MutableList<String>,
) : PendingQualificationForecastStore {
    override fun find(code: String): PendingQualificationForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingQualificationForecast) {
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

    override fun list(): MutableList<PendingQualificationForecast> {
        val out = mutableListOf<PendingQualificationForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingQualificationForecastStore(): PendingQualificationForecastStore {
    return MemoryPendingQualificationForecastStore(
        mutableMapOf<String, PendingQualificationForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingQualificationForecastStore(
    var inner: PendingQualificationForecastStore,
    var cache: TtlCache<PendingQualificationForecast>,
    var hits: Int,
    var misses: Int,
) : PendingQualificationForecastStore {
    override fun find(code: String): PendingQualificationForecast? {
        val hit = (cache.get(code) as PendingQualificationForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingQualificationForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingQualificationForecast> = inner.list()
}

fun newCachedPendingQualificationForecastStore(inner: PendingQualificationForecastStore, ttl: Int, clock: Clock): PendingQualificationForecastStore {
    return CachedPendingQualificationForecastStore(
        inner = inner,
        cache = newTtlCache<PendingQualificationForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending qualification forecasts. Empty fields match all. */
data class PendingQualificationForecastQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingQualificationForecast): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingQualificationForecast>): MutableList<PendingQualificationForecast> {
        val matched = filter(items) { candidate: PendingQualificationForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingQualificationForecasts(store: PendingQualificationForecastStore, query: PendingQualificationForecastQuery): MutableList<PendingQualificationForecast> {
    return query.apply(store.list())
}
