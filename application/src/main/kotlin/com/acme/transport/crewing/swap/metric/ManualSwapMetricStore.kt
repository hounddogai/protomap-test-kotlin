package com.acme.transport.crewing.swap.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual swap metrics by code. */
interface ManualSwapMetricStore {
    fun find(code: String): ManualSwapMetric?
    fun save(item: ManualSwapMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualSwapMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualSwapMetricStore(
    var items: MutableMap<String, ManualSwapMetric>,
    var order: MutableList<String>,
) : ManualSwapMetricStore {
    override fun find(code: String): ManualSwapMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualSwapMetric) {
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

    override fun list(): MutableList<ManualSwapMetric> {
        val out = mutableListOf<ManualSwapMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualSwapMetricStore(): ManualSwapMetricStore {
    return MemoryManualSwapMetricStore(mutableMapOf<String, ManualSwapMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualSwapMetricStore(
    var inner: ManualSwapMetricStore,
    var cache: TtlCache<ManualSwapMetric>,
    var hits: Int,
    var misses: Int,
) : ManualSwapMetricStore {
    override fun find(code: String): ManualSwapMetric? {
        val hit = (cache.get(code) as ManualSwapMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualSwapMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualSwapMetric> = inner.list()
}

fun newCachedManualSwapMetricStore(inner: ManualSwapMetricStore, ttl: Int, clock: Clock): ManualSwapMetricStore {
    return CachedManualSwapMetricStore(
        inner = inner,
        cache = newTtlCache<ManualSwapMetric>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual swap metrics. Empty fields match all. */
data class ManualSwapMetricQuery(
    var depot: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualSwapMetric): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualSwapMetric>): MutableList<ManualSwapMetric> {
        val matched = filter(items) { candidate: ManualSwapMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualSwapMetrics(store: ManualSwapMetricStore, query: ManualSwapMetricQuery): MutableList<ManualSwapMetric> {
    return query.apply(store.list())
}
