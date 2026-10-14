package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor metrics by code. */
interface EmissionFactorMetricStore {
    fun find(code: String): EmissionFactorMetric?
    fun save(item: EmissionFactorMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorMetricStore(
    var items: MutableMap<String, EmissionFactorMetric>,
    var order: MutableList<String>,
) : EmissionFactorMetricStore {
    override fun find(code: String): EmissionFactorMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorMetric) {
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

    override fun list(): MutableList<EmissionFactorMetric> {
        val out = mutableListOf<EmissionFactorMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorMetricStore(): EmissionFactorMetricStore {
    return MemoryEmissionFactorMetricStore(
        mutableMapOf<String, EmissionFactorMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorMetricStore(
    var inner: EmissionFactorMetricStore,
    var cache: TtlCache<EmissionFactorMetric>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorMetricStore {
    override fun find(code: String): EmissionFactorMetric? {
        val hit = (cache.get(code) as EmissionFactorMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorMetric> = inner.list()
}

fun newCachedEmissionFactorMetricStore(inner: EmissionFactorMetricStore, ttl: Int, clock: Clock): EmissionFactorMetricStore {
    return CachedEmissionFactorMetricStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor metrics. Empty fields match all. */
data class EmissionFactorMetricQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorMetric): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorMetric>): MutableList<EmissionFactorMetric> {
        val matched = filter(items) { candidate: EmissionFactorMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorMetrics(store: EmissionFactorMetricStore, query: EmissionFactorMetricQuery): MutableList<EmissionFactorMetric> {
    return query.apply(store.list())
}
