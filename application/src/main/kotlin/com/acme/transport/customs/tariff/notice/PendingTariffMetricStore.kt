package com.acme.transport.customs.tariff.notice

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending tariff metrics by code. */
interface PendingTariffMetricStore {
    fun find(code: String): PendingTariffMetric?
    fun save(item: PendingTariffMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingTariffMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingTariffMetricStore(
    var items: MutableMap<String, PendingTariffMetric>,
    var order: MutableList<String>,
) : PendingTariffMetricStore {
    override fun find(code: String): PendingTariffMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingTariffMetric) {
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

    override fun list(): MutableList<PendingTariffMetric> {
        val out = mutableListOf<PendingTariffMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingTariffMetricStore(): PendingTariffMetricStore {
    return MemoryPendingTariffMetricStore(mutableMapOf<String, PendingTariffMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingTariffMetricStore(
    var inner: PendingTariffMetricStore,
    var cache: TtlCache<PendingTariffMetric>,
    var hits: Int,
    var misses: Int,
) : PendingTariffMetricStore {
    override fun find(code: String): PendingTariffMetric? {
        val hit = (cache.get(code) as PendingTariffMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingTariffMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingTariffMetric> = inner.list()
}

fun newCachedPendingTariffMetricStore(inner: PendingTariffMetricStore, ttl: Int, clock: Clock): PendingTariffMetricStore {
    return CachedPendingTariffMetricStore(
        inner = inner,
        cache = newTtlCache<PendingTariffMetric>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending tariff metrics. Empty fields match all. */
data class PendingTariffMetricQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingTariffMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingTariffMetric>): MutableList<PendingTariffMetric> {
        val matched = filter(items) { candidate: PendingTariffMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingTariffMetrics(store: PendingTariffMetricStore, query: PendingTariffMetricQuery): MutableList<PendingTariffMetric> {
    return query.apply(store.list())
}
