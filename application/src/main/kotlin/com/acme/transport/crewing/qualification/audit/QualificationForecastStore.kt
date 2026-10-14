package com.acme.transport.crewing.qualification.audit

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification forecasts by code. */
interface QualificationForecastStore {
    fun find(code: String): QualificationForecast?
    fun save(item: QualificationForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationForecastStore(
    var items: MutableMap<String, QualificationForecast>,
    var order: MutableList<String>,
) : QualificationForecastStore {
    override fun find(code: String): QualificationForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationForecast) {
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

    override fun list(): MutableList<QualificationForecast> {
        val out = mutableListOf<QualificationForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationForecastStore(): QualificationForecastStore {
    return MemoryQualificationForecastStore(
        mutableMapOf<String, QualificationForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationForecastStore(
    var inner: QualificationForecastStore,
    var cache: TtlCache<QualificationForecast>,
    var hits: Int,
    var misses: Int,
) : QualificationForecastStore {
    override fun find(code: String): QualificationForecast? {
        val hit = (cache.get(code) as QualificationForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationForecast> = inner.list()
}

fun newCachedQualificationForecastStore(inner: QualificationForecastStore, ttl: Int, clock: Clock): QualificationForecastStore {
    return CachedQualificationForecastStore(
        inner = inner,
        cache = newTtlCache<QualificationForecast>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification forecasts. Empty fields match all. */
data class QualificationForecastQuery(
    var country: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationForecast): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<QualificationForecast>): MutableList<QualificationForecast> {
        val matched = filter(items) { candidate: QualificationForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationForecasts(store: QualificationForecastStore, query: QualificationForecastQuery): MutableList<QualificationForecast> {
    return query.apply(store.list())
}
