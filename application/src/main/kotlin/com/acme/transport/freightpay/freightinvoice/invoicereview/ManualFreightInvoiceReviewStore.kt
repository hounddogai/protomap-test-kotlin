package com.acme.transport.freightpay.freightinvoice.invoicereview

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual freight invoice reviews by code. */
interface ManualFreightInvoiceReviewStore {
    fun find(code: String): ManualFreightInvoiceReview?
    fun save(item: ManualFreightInvoiceReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualFreightInvoiceReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualFreightInvoiceReviewStore(
    var items: MutableMap<String, ManualFreightInvoiceReview>,
    var order: MutableList<String>,
) : ManualFreightInvoiceReviewStore {
    override fun find(code: String): ManualFreightInvoiceReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualFreightInvoiceReview) {
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

    override fun list(): MutableList<ManualFreightInvoiceReview> {
        val out = mutableListOf<ManualFreightInvoiceReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualFreightInvoiceReviewStore(): ManualFreightInvoiceReviewStore {
    return MemoryManualFreightInvoiceReviewStore(
        mutableMapOf<String, ManualFreightInvoiceReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualFreightInvoiceReviewStore(
    var inner: ManualFreightInvoiceReviewStore,
    var cache: TtlCache<ManualFreightInvoiceReview>,
    var hits: Int,
    var misses: Int,
) : ManualFreightInvoiceReviewStore {
    override fun find(code: String): ManualFreightInvoiceReview? {
        val hit = (cache.get(code) as ManualFreightInvoiceReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualFreightInvoiceReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualFreightInvoiceReview> = inner.list()
}

fun newCachedManualFreightInvoiceReviewStore(inner: ManualFreightInvoiceReviewStore, ttl: Int, clock: Clock): ManualFreightInvoiceReviewStore {
    return CachedManualFreightInvoiceReviewStore(
        inner = inner,
        cache = newTtlCache<ManualFreightInvoiceReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual freight invoice reviews. Empty fields match all. */
data class ManualFreightInvoiceReviewQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualFreightInvoiceReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualFreightInvoiceReview>): MutableList<ManualFreightInvoiceReview> {
        val matched = filter(items) { candidate: ManualFreightInvoiceReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualFreightInvoiceReviews(store: ManualFreightInvoiceReviewStore, query: ManualFreightInvoiceReviewQuery): MutableList<ManualFreightInvoiceReview> {
    return query.apply(store.list())
}
