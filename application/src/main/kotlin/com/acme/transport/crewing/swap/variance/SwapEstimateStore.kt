package com.acme.transport.crewing.swap.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap estimates by code. */
interface SwapEstimateStore {
    fun find(code: String): SwapEstimate?
    fun save(item: SwapEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapEstimateStore(
    var items: MutableMap<String, SwapEstimate>,
    var order: MutableList<String>,
) : SwapEstimateStore {
    override fun find(code: String): SwapEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapEstimate) {
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

    override fun list(): MutableList<SwapEstimate> {
        val out = mutableListOf<SwapEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapEstimateStore(): SwapEstimateStore {
    return MemorySwapEstimateStore(mutableMapOf<String, SwapEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapEstimateStore(
    var inner: SwapEstimateStore,
    var cache: TtlCache<SwapEstimate>,
    var hits: Int,
    var misses: Int,
) : SwapEstimateStore {
    override fun find(code: String): SwapEstimate? {
        val hit = (cache.get(code) as SwapEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapEstimate> = inner.list()
}

fun newCachedSwapEstimateStore(inner: SwapEstimateStore, ttl: Int, clock: Clock): SwapEstimateStore {
    return CachedSwapEstimateStore(
        inner = inner,
        cache = newTtlCache<SwapEstimate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap estimates. Empty fields match all. */
data class SwapEstimateQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapEstimate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapEstimate>): MutableList<SwapEstimate> {
        val matched = filter(items) { candidate: SwapEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapEstimates(store: SwapEstimateStore, query: SwapEstimateQuery): MutableList<SwapEstimate> {
    return query.apply(store.list())
}
