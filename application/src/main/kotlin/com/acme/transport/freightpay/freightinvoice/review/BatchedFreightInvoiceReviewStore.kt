package com.acme.transport.freightpay.freightinvoice.review

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched freight invoice reviews by code. */
interface BatchedFreightInvoiceReviewStore {
    fun find(code: String): BatchedFreightInvoiceReview?
    fun save(item: BatchedFreightInvoiceReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedFreightInvoiceReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedFreightInvoiceReviewStore(
    var items: MutableMap<String, BatchedFreightInvoiceReview>,
    var order: MutableList<String>,
) : BatchedFreightInvoiceReviewStore {
    override fun find(code: String): BatchedFreightInvoiceReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedFreightInvoiceReview) {
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

    override fun list(): MutableList<BatchedFreightInvoiceReview> {
        val out = mutableListOf<BatchedFreightInvoiceReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedFreightInvoiceReviewStore(): BatchedFreightInvoiceReviewStore {
    return MemoryBatchedFreightInvoiceReviewStore(
        mutableMapOf<String, BatchedFreightInvoiceReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedFreightInvoiceReviewStore(
    var inner: BatchedFreightInvoiceReviewStore,
    var cache: TtlCache<BatchedFreightInvoiceReview>,
    var hits: Int,
    var misses: Int,
) : BatchedFreightInvoiceReviewStore {
    override fun find(code: String): BatchedFreightInvoiceReview? {
        val hit = (cache.get(code) as BatchedFreightInvoiceReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedFreightInvoiceReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedFreightInvoiceReview> = inner.list()
}

fun newCachedBatchedFreightInvoiceReviewStore(inner: BatchedFreightInvoiceReviewStore, ttl: Int, clock: Clock): BatchedFreightInvoiceReviewStore {
    return CachedBatchedFreightInvoiceReviewStore(
        inner = inner,
        cache = newTtlCache<BatchedFreightInvoiceReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched freight invoice reviews. Empty fields match all. */
data class BatchedFreightInvoiceReviewQuery(
    var source: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedFreightInvoiceReview): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedFreightInvoiceReview>): MutableList<BatchedFreightInvoiceReview> {
        val matched = filter(items) { candidate: BatchedFreightInvoiceReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedFreightInvoiceReviews(store: BatchedFreightInvoiceReviewStore, query: BatchedFreightInvoiceReviewQuery): MutableList<BatchedFreightInvoiceReview> {
    return query.apply(store.list())
}
