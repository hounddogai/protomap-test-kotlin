package com.acme.transport.customs.inspection.pendinginspectionrule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default inspection forecasts by code. */
interface DefaultInspectionForecastStore {
    fun find(code: String): DefaultInspectionForecast?
    fun save(item: DefaultInspectionForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultInspectionForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultInspectionForecastStore(
    var items: MutableMap<String, DefaultInspectionForecast>,
    var order: MutableList<String>,
) : DefaultInspectionForecastStore {
    override fun find(code: String): DefaultInspectionForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultInspectionForecast) {
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

    override fun list(): MutableList<DefaultInspectionForecast> {
        val out = mutableListOf<DefaultInspectionForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultInspectionForecastStore(): DefaultInspectionForecastStore {
    return MemoryDefaultInspectionForecastStore(
        mutableMapOf<String, DefaultInspectionForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultInspectionForecastStore(
    var inner: DefaultInspectionForecastStore,
    var cache: TtlCache<DefaultInspectionForecast>,
    var hits: Int,
    var misses: Int,
) : DefaultInspectionForecastStore {
    override fun find(code: String): DefaultInspectionForecast? {
        val hit = (cache.get(code) as DefaultInspectionForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultInspectionForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultInspectionForecast> = inner.list()
}

fun newCachedDefaultInspectionForecastStore(inner: DefaultInspectionForecastStore, ttl: Int, clock: Clock): DefaultInspectionForecastStore {
    return CachedDefaultInspectionForecastStore(
        inner = inner,
        cache = newTtlCache<DefaultInspectionForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default inspection forecasts. Empty fields match all. */
data class DefaultInspectionForecastQuery(
    var name: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultInspectionForecast): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<DefaultInspectionForecast>): MutableList<DefaultInspectionForecast> {
        val matched = filter(items) { candidate: DefaultInspectionForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultInspectionForecasts(store: DefaultInspectionForecastStore, query: DefaultInspectionForecastQuery): MutableList<DefaultInspectionForecast> {
    return query.apply(store.list())
}
