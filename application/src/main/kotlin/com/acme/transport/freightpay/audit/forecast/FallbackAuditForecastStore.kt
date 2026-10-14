package com.acme.transport.freightpay.audit.forecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback audit forecasts by code. */
interface FallbackAuditForecastStore {
    fun find(code: String): FallbackAuditForecast?
    fun save(item: FallbackAuditForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAuditForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAuditForecastStore(
    var items: MutableMap<String, FallbackAuditForecast>,
    var order: MutableList<String>,
) : FallbackAuditForecastStore {
    override fun find(code: String): FallbackAuditForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAuditForecast) {
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

    override fun list(): MutableList<FallbackAuditForecast> {
        val out = mutableListOf<FallbackAuditForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAuditForecastStore(): FallbackAuditForecastStore {
    return MemoryFallbackAuditForecastStore(
        mutableMapOf<String, FallbackAuditForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAuditForecastStore(
    var inner: FallbackAuditForecastStore,
    var cache: TtlCache<FallbackAuditForecast>,
    var hits: Int,
    var misses: Int,
) : FallbackAuditForecastStore {
    override fun find(code: String): FallbackAuditForecast? {
        val hit = (cache.get(code) as FallbackAuditForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAuditForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAuditForecast> = inner.list()
}

fun newCachedFallbackAuditForecastStore(inner: FallbackAuditForecastStore, ttl: Int, clock: Clock): FallbackAuditForecastStore {
    return CachedFallbackAuditForecastStore(
        inner = inner,
        cache = newTtlCache<FallbackAuditForecast>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback audit forecasts. Empty fields match all. */
data class FallbackAuditForecastQuery(
    var region: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAuditForecast): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAuditForecast>): MutableList<FallbackAuditForecast> {
        val matched = filter(items) { candidate: FallbackAuditForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAuditForecasts(store: FallbackAuditForecastStore, query: FallbackAuditForecastQuery): MutableList<FallbackAuditForecast> {
    return query.apply(store.list())
}
