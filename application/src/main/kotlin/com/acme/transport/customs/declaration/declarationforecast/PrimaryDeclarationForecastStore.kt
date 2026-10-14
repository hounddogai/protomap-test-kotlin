package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary declaration forecasts by code. */
interface PrimaryDeclarationForecastStore {
    fun find(code: String): PrimaryDeclarationForecast?
    fun save(item: PrimaryDeclarationForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryDeclarationForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryDeclarationForecastStore(
    var items: MutableMap<String, PrimaryDeclarationForecast>,
    var order: MutableList<String>,
) : PrimaryDeclarationForecastStore {
    override fun find(code: String): PrimaryDeclarationForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryDeclarationForecast) {
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

    override fun list(): MutableList<PrimaryDeclarationForecast> {
        val out = mutableListOf<PrimaryDeclarationForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryDeclarationForecastStore(): PrimaryDeclarationForecastStore {
    return MemoryPrimaryDeclarationForecastStore(
        mutableMapOf<String, PrimaryDeclarationForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryDeclarationForecastStore(
    var inner: PrimaryDeclarationForecastStore,
    var cache: TtlCache<PrimaryDeclarationForecast>,
    var hits: Int,
    var misses: Int,
) : PrimaryDeclarationForecastStore {
    override fun find(code: String): PrimaryDeclarationForecast? {
        val hit = (cache.get(code) as PrimaryDeclarationForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryDeclarationForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryDeclarationForecast> = inner.list()
}

fun newCachedPrimaryDeclarationForecastStore(inner: PrimaryDeclarationForecastStore, ttl: Int, clock: Clock): PrimaryDeclarationForecastStore {
    return CachedPrimaryDeclarationForecastStore(
        inner = inner,
        cache = newTtlCache<PrimaryDeclarationForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary declaration forecasts. Empty fields match all. */
data class PrimaryDeclarationForecastQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryDeclarationForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<PrimaryDeclarationForecast>): MutableList<PrimaryDeclarationForecast> {
        val matched = filter(items) { candidate: PrimaryDeclarationForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryDeclarationForecasts(store: PrimaryDeclarationForecastStore, query: PrimaryDeclarationForecastQuery): MutableList<PrimaryDeclarationForecast> {
    return query.apply(store.list())
}
