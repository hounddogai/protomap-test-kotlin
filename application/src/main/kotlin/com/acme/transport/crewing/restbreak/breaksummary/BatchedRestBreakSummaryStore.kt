package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched rest break summaries by code. */
interface BatchedRestBreakSummaryStore {
    fun find(code: String): BatchedRestBreakSummary?
    fun save(item: BatchedRestBreakSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRestBreakSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRestBreakSummaryStore(
    var items: MutableMap<String, BatchedRestBreakSummary>,
    var order: MutableList<String>,
) : BatchedRestBreakSummaryStore {
    override fun find(code: String): BatchedRestBreakSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRestBreakSummary) {
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

    override fun list(): MutableList<BatchedRestBreakSummary> {
        val out = mutableListOf<BatchedRestBreakSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRestBreakSummaryStore(): BatchedRestBreakSummaryStore {
    return MemoryBatchedRestBreakSummaryStore(
        mutableMapOf<String, BatchedRestBreakSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRestBreakSummaryStore(
    var inner: BatchedRestBreakSummaryStore,
    var cache: TtlCache<BatchedRestBreakSummary>,
    var hits: Int,
    var misses: Int,
) : BatchedRestBreakSummaryStore {
    override fun find(code: String): BatchedRestBreakSummary? {
        val hit = (cache.get(code) as BatchedRestBreakSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRestBreakSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRestBreakSummary> = inner.list()
}

fun newCachedBatchedRestBreakSummaryStore(inner: BatchedRestBreakSummaryStore, ttl: Int, clock: Clock): BatchedRestBreakSummaryStore {
    return CachedBatchedRestBreakSummaryStore(
        inner = inner,
        cache = newTtlCache<BatchedRestBreakSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched rest break summaries. Empty fields match all. */
data class BatchedRestBreakSummaryQuery(
    var name: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRestBreakSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRestBreakSummary>): MutableList<BatchedRestBreakSummary> {
        val matched = filter(items) { candidate: BatchedRestBreakSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRestBreakSummaries(store: BatchedRestBreakSummaryStore, query: BatchedRestBreakSummaryQuery): MutableList<BatchedRestBreakSummary> {
    return query.apply(store.list())
}
