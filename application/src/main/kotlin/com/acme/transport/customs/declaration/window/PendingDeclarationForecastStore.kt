package com.acme.transport.customs.declaration.window

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending declaration forecasts by code. */
interface PendingDeclarationForecastStore {
    fun find(code: String): PendingDeclarationForecast?
    fun save(item: PendingDeclarationForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDeclarationForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDeclarationForecastStore(
    var items: MutableMap<String, PendingDeclarationForecast>,
    var order: MutableList<String>,
) : PendingDeclarationForecastStore {
    override fun find(code: String): PendingDeclarationForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDeclarationForecast) {
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

    override fun list(): MutableList<PendingDeclarationForecast> {
        val out = mutableListOf<PendingDeclarationForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDeclarationForecastStore(): PendingDeclarationForecastStore {
    return MemoryPendingDeclarationForecastStore(
        mutableMapOf<String, PendingDeclarationForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDeclarationForecastStore(
    var inner: PendingDeclarationForecastStore,
    var cache: TtlCache<PendingDeclarationForecast>,
    var hits: Int,
    var misses: Int,
) : PendingDeclarationForecastStore {
    override fun find(code: String): PendingDeclarationForecast? {
        val hit = (cache.get(code) as PendingDeclarationForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDeclarationForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDeclarationForecast> = inner.list()
}

fun newCachedPendingDeclarationForecastStore(inner: PendingDeclarationForecastStore, ttl: Int, clock: Clock): PendingDeclarationForecastStore {
    return CachedPendingDeclarationForecastStore(
        inner = inner,
        cache = newTtlCache<PendingDeclarationForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending declaration forecasts. Empty fields match all. */
data class PendingDeclarationForecastQuery(
    var category: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDeclarationForecast): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDeclarationForecast>): MutableList<PendingDeclarationForecast> {
        val matched = filter(items) { candidate: PendingDeclarationForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDeclarationForecasts(store: PendingDeclarationForecastStore, query: PendingDeclarationForecastQuery): MutableList<PendingDeclarationForecast> {
    return query.apply(store.list())
}
