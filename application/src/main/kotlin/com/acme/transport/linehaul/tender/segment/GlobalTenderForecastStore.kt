package com.acme.transport.linehaul.tender.segment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global tender forecasts by code. */
interface GlobalTenderForecastStore {
    fun find(code: String): GlobalTenderForecast?
    fun save(item: GlobalTenderForecast)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTenderForecast>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTenderForecastStore(
    var items: MutableMap<String, GlobalTenderForecast>,
    var order: MutableList<String>,
) : GlobalTenderForecastStore {
    override fun find(code: String): GlobalTenderForecast? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTenderForecast) {
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

    override fun list(): MutableList<GlobalTenderForecast> {
        val out = mutableListOf<GlobalTenderForecast>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTenderForecastStore(): GlobalTenderForecastStore {
    return MemoryGlobalTenderForecastStore(
        mutableMapOf<String, GlobalTenderForecast>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTenderForecastStore(
    var inner: GlobalTenderForecastStore,
    var cache: TtlCache<GlobalTenderForecast>,
    var hits: Int,
    var misses: Int,
) : GlobalTenderForecastStore {
    override fun find(code: String): GlobalTenderForecast? {
        val hit = (cache.get(code) as GlobalTenderForecast?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTenderForecast) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTenderForecast> = inner.list()
}

fun newCachedGlobalTenderForecastStore(inner: GlobalTenderForecastStore, ttl: Int, clock: Clock): GlobalTenderForecastStore {
    return CachedGlobalTenderForecastStore(
        inner = inner,
        cache = newTtlCache<GlobalTenderForecast>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global tender forecasts. Empty fields match all. */
data class GlobalTenderForecastQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTenderForecast): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalTenderForecast>): MutableList<GlobalTenderForecast> {
        val matched = filter(items) { candidate: GlobalTenderForecast -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTenderForecasts(store: GlobalTenderForecastStore, query: GlobalTenderForecastQuery): MutableList<GlobalTenderForecast> {
    return query.apply(store.list())
}
