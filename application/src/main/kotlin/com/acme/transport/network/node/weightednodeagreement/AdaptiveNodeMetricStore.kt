package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps adaptive node metrics by code. */
interface AdaptiveNodeMetricStore {
    fun find(code: String): AdaptiveNodeMetric?
    fun save(item: AdaptiveNodeMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveNodeMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveNodeMetricStore(
    var items: MutableMap<String, AdaptiveNodeMetric>,
    var order: MutableList<String>,
) : AdaptiveNodeMetricStore {
    override fun find(code: String): AdaptiveNodeMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveNodeMetric) {
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

    override fun list(): MutableList<AdaptiveNodeMetric> {
        val out = mutableListOf<AdaptiveNodeMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveNodeMetricStore(): AdaptiveNodeMetricStore {
    return MemoryAdaptiveNodeMetricStore(mutableMapOf<String, AdaptiveNodeMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveNodeMetricStore(
    var inner: AdaptiveNodeMetricStore,
    var cache: TtlCache<AdaptiveNodeMetric>,
    var hits: Int,
    var misses: Int,
) : AdaptiveNodeMetricStore {
    override fun find(code: String): AdaptiveNodeMetric? {
        val hit = (cache.get(code) as AdaptiveNodeMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveNodeMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveNodeMetric> = inner.list()
}

fun newCachedAdaptiveNodeMetricStore(inner: AdaptiveNodeMetricStore, ttl: Int, clock: Clock): AdaptiveNodeMetricStore {
    return CachedAdaptiveNodeMetricStore(
        inner = inner,
        cache = newTtlCache<AdaptiveNodeMetric>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive node metrics. Empty fields match all. */
data class AdaptiveNodeMetricQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveNodeMetric): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveNodeMetric>): MutableList<AdaptiveNodeMetric> {
        val matched = filter(items) { candidate: AdaptiveNodeMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveNodeMetrics(store: AdaptiveNodeMetricStore, query: AdaptiveNodeMetricQuery): MutableList<AdaptiveNodeMetric> {
    return query.apply(store.list())
}
