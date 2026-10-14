package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary fuel burn metrics by code. */
interface PrimaryFuelBurnMetricStore {
    fun find(code: String): PrimaryFuelBurnMetric?
    fun save(item: PrimaryFuelBurnMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryFuelBurnMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryFuelBurnMetricStore(
    var items: MutableMap<String, PrimaryFuelBurnMetric>,
    var order: MutableList<String>,
) : PrimaryFuelBurnMetricStore {
    override fun find(code: String): PrimaryFuelBurnMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryFuelBurnMetric) {
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

    override fun list(): MutableList<PrimaryFuelBurnMetric> {
        val out = mutableListOf<PrimaryFuelBurnMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryFuelBurnMetricStore(): PrimaryFuelBurnMetricStore {
    return MemoryPrimaryFuelBurnMetricStore(
        mutableMapOf<String, PrimaryFuelBurnMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryFuelBurnMetricStore(
    var inner: PrimaryFuelBurnMetricStore,
    var cache: TtlCache<PrimaryFuelBurnMetric>,
    var hits: Int,
    var misses: Int,
) : PrimaryFuelBurnMetricStore {
    override fun find(code: String): PrimaryFuelBurnMetric? {
        val hit = (cache.get(code) as PrimaryFuelBurnMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryFuelBurnMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryFuelBurnMetric> = inner.list()
}

fun newCachedPrimaryFuelBurnMetricStore(inner: PrimaryFuelBurnMetricStore, ttl: Int, clock: Clock): PrimaryFuelBurnMetricStore {
    return CachedPrimaryFuelBurnMetricStore(
        inner = inner,
        cache = newTtlCache<PrimaryFuelBurnMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary fuel burn metrics. Empty fields match all. */
data class PrimaryFuelBurnMetricQuery(
    var unit: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryFuelBurnMetric): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryFuelBurnMetric>): MutableList<PrimaryFuelBurnMetric> {
        val matched = filter(items) { candidate: PrimaryFuelBurnMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryFuelBurnMetrics(store: PrimaryFuelBurnMetricStore, query: PrimaryFuelBurnMetricQuery): MutableList<PrimaryFuelBurnMetric> {
    return query.apply(store.list())
}
