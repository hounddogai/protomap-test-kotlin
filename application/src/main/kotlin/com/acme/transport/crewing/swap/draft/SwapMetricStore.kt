package com.acme.transport.crewing.swap.draft

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap metrics by code. */
interface SwapMetricStore {
    fun find(code: String): SwapMetric?
    fun save(item: SwapMetric)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapMetric>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapMetricStore(
    var items: MutableMap<String, SwapMetric>,
    var order: MutableList<String>,
) : SwapMetricStore {
    override fun find(code: String): SwapMetric? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapMetric) {
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

    override fun list(): MutableList<SwapMetric> {
        val out = mutableListOf<SwapMetric>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapMetricStore(): SwapMetricStore {
    return MemorySwapMetricStore(mutableMapOf<String, SwapMetric>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapMetricStore(
    var inner: SwapMetricStore,
    var cache: TtlCache<SwapMetric>,
    var hits: Int,
    var misses: Int,
) : SwapMetricStore {
    override fun find(code: String): SwapMetric? {
        val hit = (cache.get(code) as SwapMetric?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapMetric) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapMetric> = inner.list()
}

fun newCachedSwapMetricStore(inner: SwapMetricStore, ttl: Int, clock: Clock): SwapMetricStore {
    return CachedSwapMetricStore(
        inner = inner,
        cache = newTtlCache<SwapMetric>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap metrics. Empty fields match all. */
data class SwapMetricQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapMetric): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapMetric>): MutableList<SwapMetric> {
        val matched = filter(items) { candidate: SwapMetric -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapMetrics(store: SwapMetricStore, query: SwapMetricQuery): MutableList<SwapMetric> {
    return query.apply(store.list())
}
