package com.acme.transport.crewing.availability.adjustment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending availability reviews by code. */
interface PendingAvailabilityReviewStore {
    fun find(code: String): PendingAvailabilityReview?
    fun save(item: PendingAvailabilityReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingAvailabilityReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingAvailabilityReviewStore(
    var items: MutableMap<String, PendingAvailabilityReview>,
    var order: MutableList<String>,
) : PendingAvailabilityReviewStore {
    override fun find(code: String): PendingAvailabilityReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingAvailabilityReview) {
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

    override fun list(): MutableList<PendingAvailabilityReview> {
        val out = mutableListOf<PendingAvailabilityReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingAvailabilityReviewStore(): PendingAvailabilityReviewStore {
    return MemoryPendingAvailabilityReviewStore(
        mutableMapOf<String, PendingAvailabilityReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingAvailabilityReviewStore(
    var inner: PendingAvailabilityReviewStore,
    var cache: TtlCache<PendingAvailabilityReview>,
    var hits: Int,
    var misses: Int,
) : PendingAvailabilityReviewStore {
    override fun find(code: String): PendingAvailabilityReview? {
        val hit = (cache.get(code) as PendingAvailabilityReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingAvailabilityReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingAvailabilityReview> = inner.list()
}

fun newCachedPendingAvailabilityReviewStore(inner: PendingAvailabilityReviewStore, ttl: Int, clock: Clock): PendingAvailabilityReviewStore {
    return CachedPendingAvailabilityReviewStore(
        inner = inner,
        cache = newTtlCache<PendingAvailabilityReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending availability reviews. Empty fields match all. */
data class PendingAvailabilityReviewQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingAvailabilityReview): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<PendingAvailabilityReview>): MutableList<PendingAvailabilityReview> {
        val matched = filter(items) { candidate: PendingAvailabilityReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingAvailabilityReviews(store: PendingAvailabilityReviewStore, query: PendingAvailabilityReviewQuery): MutableList<PendingAvailabilityReview> {
    return query.apply(store.list())
}
