package com.acme.transport.crewing.swap.metric

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap summaries by code. */
interface SwapSummaryStore {
    fun find(code: String): SwapSummary?
    fun save(item: SwapSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapSummaryStore(
    var items: MutableMap<String, SwapSummary>,
    var order: MutableList<String>,
) : SwapSummaryStore {
    override fun find(code: String): SwapSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapSummary) {
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

    override fun list(): MutableList<SwapSummary> {
        val out = mutableListOf<SwapSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapSummaryStore(): SwapSummaryStore {
    return MemorySwapSummaryStore(mutableMapOf<String, SwapSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapSummaryStore(
    var inner: SwapSummaryStore,
    var cache: TtlCache<SwapSummary>,
    var hits: Int,
    var misses: Int,
) : SwapSummaryStore {
    override fun find(code: String): SwapSummary? {
        val hit = (cache.get(code) as SwapSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapSummary> = inner.list()
}

fun newCachedSwapSummaryStore(inner: SwapSummaryStore, ttl: Int, clock: Clock): SwapSummaryStore {
    return CachedSwapSummaryStore(
        inner = inner,
        cache = newTtlCache<SwapSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap summaries. Empty fields match all. */
data class SwapSummaryQuery(
    var segment: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapSummary): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapSummary>): MutableList<SwapSummary> {
        val matched = filter(items) { candidate: SwapSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapSummaries(store: SwapSummaryStore, query: SwapSummaryQuery): MutableList<SwapSummary> {
    return query.apply(store.list())
}
