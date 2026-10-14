package com.acme.transport.crewing.restbreak.adjustment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched rest break reviews by code. */
interface BatchedRestBreakReviewStore {
    fun find(code: String): BatchedRestBreakReview?
    fun save(item: BatchedRestBreakReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRestBreakReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRestBreakReviewStore(
    var items: MutableMap<String, BatchedRestBreakReview>,
    var order: MutableList<String>,
) : BatchedRestBreakReviewStore {
    override fun find(code: String): BatchedRestBreakReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRestBreakReview) {
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

    override fun list(): MutableList<BatchedRestBreakReview> {
        val out = mutableListOf<BatchedRestBreakReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRestBreakReviewStore(): BatchedRestBreakReviewStore {
    return MemoryBatchedRestBreakReviewStore(
        mutableMapOf<String, BatchedRestBreakReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRestBreakReviewStore(
    var inner: BatchedRestBreakReviewStore,
    var cache: TtlCache<BatchedRestBreakReview>,
    var hits: Int,
    var misses: Int,
) : BatchedRestBreakReviewStore {
    override fun find(code: String): BatchedRestBreakReview? {
        val hit = (cache.get(code) as BatchedRestBreakReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRestBreakReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRestBreakReview> = inner.list()
}

fun newCachedBatchedRestBreakReviewStore(inner: BatchedRestBreakReviewStore, ttl: Int, clock: Clock): BatchedRestBreakReviewStore {
    return CachedBatchedRestBreakReviewStore(
        inner = inner,
        cache = newTtlCache<BatchedRestBreakReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched rest break reviews. Empty fields match all. */
data class BatchedRestBreakReviewQuery(
    var label: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRestBreakReview): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRestBreakReview>): MutableList<BatchedRestBreakReview> {
        val matched = filter(items) { candidate: BatchedRestBreakReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRestBreakReviews(store: BatchedRestBreakReviewStore, query: BatchedRestBreakReviewQuery): MutableList<BatchedRestBreakReview> {
    return query.apply(store.list())
}
