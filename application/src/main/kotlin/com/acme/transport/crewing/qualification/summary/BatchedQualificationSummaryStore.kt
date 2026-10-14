package com.acme.transport.crewing.qualification.summary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched qualification summaries by code. */
interface BatchedQualificationSummaryStore {
    fun find(code: String): BatchedQualificationSummary?
    fun save(item: BatchedQualificationSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedQualificationSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedQualificationSummaryStore(
    var items: MutableMap<String, BatchedQualificationSummary>,
    var order: MutableList<String>,
) : BatchedQualificationSummaryStore {
    override fun find(code: String): BatchedQualificationSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedQualificationSummary) {
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

    override fun list(): MutableList<BatchedQualificationSummary> {
        val out = mutableListOf<BatchedQualificationSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedQualificationSummaryStore(): BatchedQualificationSummaryStore {
    return MemoryBatchedQualificationSummaryStore(
        mutableMapOf<String, BatchedQualificationSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedQualificationSummaryStore(
    var inner: BatchedQualificationSummaryStore,
    var cache: TtlCache<BatchedQualificationSummary>,
    var hits: Int,
    var misses: Int,
) : BatchedQualificationSummaryStore {
    override fun find(code: String): BatchedQualificationSummary? {
        val hit = (cache.get(code) as BatchedQualificationSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedQualificationSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedQualificationSummary> = inner.list()
}

fun newCachedBatchedQualificationSummaryStore(inner: BatchedQualificationSummaryStore, ttl: Int, clock: Clock): BatchedQualificationSummaryStore {
    return CachedBatchedQualificationSummaryStore(
        inner = inner,
        cache = newTtlCache<BatchedQualificationSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched qualification summaries. Empty fields match all. */
data class BatchedQualificationSummaryQuery(
    var depot: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedQualificationSummary): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedQualificationSummary>): MutableList<BatchedQualificationSummary> {
        val matched = filter(items) { candidate: BatchedQualificationSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedQualificationSummaries(store: BatchedQualificationSummaryStore, query: BatchedQualificationSummaryQuery): MutableList<BatchedQualificationSummary> {
    return query.apply(store.list())
}
