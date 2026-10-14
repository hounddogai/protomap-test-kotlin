package com.acme.transport.terminal.dockdoor.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive dock door metrics by code. */
interface AdaptiveDockDoorMetricStore {
    fun find(code: String): AdaptiveDockDoorMetric?
    fun save(item: AdaptiveDockDoorMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDockDoorMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDockDoorMetricStore(
    var items: MutableMap<String, AdaptiveDockDoorMetric>,
    var order: MutableList<String>,
) : AdaptiveDockDoorMetricStore {
    override fun find(code: String): AdaptiveDockDoorMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDockDoorMetric) {
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

    override fun list(): MutableList<AdaptiveDockDoorMetric> {
        val out = mutableListOf<AdaptiveDockDoorMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDockDoorMetricStore(): AdaptiveDockDoorMetricStore {
    return MemoryAdaptiveDockDoorMetricStore(
        mutableMapOf<String, AdaptiveDockDoorMetric>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDockDoorMetricStore(
    var inner: AdaptiveDockDoorMetricStore,
    var cache: TtlCache<AdaptiveDockDoorMetric>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDockDoorMetricStore {
    override fun find(code: String): AdaptiveDockDoorMetric? {
        val hit = (cache.get(code) as AdaptiveDockDoorMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDockDoorMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDockDoorMetric> = inner.list()
}

fun newCachedAdaptiveDockDoorMetricStore(inner: AdaptiveDockDoorMetricStore, ttl: Int, clock: Clock): AdaptiveDockDoorMetricStore {
    return CachedAdaptiveDockDoorMetricStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDockDoorMetric>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive dock door metrics. Empty fields match all. */
data class AdaptiveDockDoorMetricQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDockDoorMetric): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveDockDoorMetric>): MutableList<AdaptiveDockDoorMetric> {
        val matched = filter(items) { candidate: AdaptiveDockDoorMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDockDoorMetrics(store: AdaptiveDockDoorMetricStore, query: AdaptiveDockDoorMetricQuery): MutableList<AdaptiveDockDoorMetric> {
    return query.apply(store.list())
}
