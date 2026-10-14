package com.acme.transport.lastmile.proofofdelivery.cycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched proof of delivery reviews by code. */
interface BatchedProofOfDeliveryReviewStore {
    fun find(code: String): BatchedProofOfDeliveryReview?
    fun save(item: BatchedProofOfDeliveryReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedProofOfDeliveryReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedProofOfDeliveryReviewStore(
    var items: MutableMap<String, BatchedProofOfDeliveryReview>,
    var order: MutableList<String>,
) : BatchedProofOfDeliveryReviewStore {
    override fun find(code: String): BatchedProofOfDeliveryReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedProofOfDeliveryReview) {
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

    override fun list(): MutableList<BatchedProofOfDeliveryReview> {
        val out = mutableListOf<BatchedProofOfDeliveryReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedProofOfDeliveryReviewStore(): BatchedProofOfDeliveryReviewStore {
    return MemoryBatchedProofOfDeliveryReviewStore(
        mutableMapOf<String, BatchedProofOfDeliveryReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedProofOfDeliveryReviewStore(
    var inner: BatchedProofOfDeliveryReviewStore,
    var cache: TtlCache<BatchedProofOfDeliveryReview>,
    var hits: Int,
    var misses: Int,
) : BatchedProofOfDeliveryReviewStore {
    override fun find(code: String): BatchedProofOfDeliveryReview? {
        val hit = (cache.get(code) as BatchedProofOfDeliveryReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedProofOfDeliveryReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedProofOfDeliveryReview> = inner.list()
}

fun newCachedBatchedProofOfDeliveryReviewStore(inner: BatchedProofOfDeliveryReviewStore, ttl: Int, clock: Clock): BatchedProofOfDeliveryReviewStore {
    return CachedBatchedProofOfDeliveryReviewStore(
        inner = inner,
        cache = newTtlCache<BatchedProofOfDeliveryReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched proof of delivery reviews. Empty fields match all. */
data class BatchedProofOfDeliveryReviewQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedProofOfDeliveryReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedProofOfDeliveryReview>): MutableList<BatchedProofOfDeliveryReview> {
        val matched = filter(items) { candidate: BatchedProofOfDeliveryReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedProofOfDeliveryReviews(store: BatchedProofOfDeliveryReviewStore, query: BatchedProofOfDeliveryReviewQuery): MutableList<BatchedProofOfDeliveryReview> {
    return query.apply(store.list())
}
