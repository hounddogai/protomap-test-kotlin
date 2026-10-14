package com.acme.transport.customs.tariff.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tariff metrics by code. */
interface TariffMetricStore {
    fun find(code: String): TariffMetric?
    fun save(item: TariffMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<TariffMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTariffMetricStore(
    var items: MutableMap<String, TariffMetric>,
    var order: MutableList<String>,
) : TariffMetricStore {
    override fun find(code: String): TariffMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TariffMetric) {
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

    override fun list(): MutableList<TariffMetric> {
        val out = mutableListOf<TariffMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTariffMetricStore(): TariffMetricStore {
    return MemoryTariffMetricStore(mutableMapOf<String, TariffMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTariffMetricStore(
    var inner: TariffMetricStore,
    var cache: TtlCache<TariffMetric>,
    var hits: Int,
    var misses: Int,
) : TariffMetricStore {
    override fun find(code: String): TariffMetric? {
        val hit = (cache.get(code) as TariffMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TariffMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TariffMetric> = inner.list()
}

fun newCachedTariffMetricStore(inner: TariffMetricStore, ttl: Int, clock: Clock): TariffMetricStore {
    return CachedTariffMetricStore(
        inner = inner,
        cache = newTtlCache<TariffMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tariff metrics. Empty fields match all. */
data class TariffMetricQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TariffMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TariffMetric>): MutableList<TariffMetric> {
        val matched = filter(items) { candidate: TariffMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTariffMetrics(store: TariffMetricStore, query: TariffMetricQuery): MutableList<TariffMetric> {
    return query.apply(store.list())
}
