package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity reviews by code. */
interface CapacityReviewStore {
    fun find(code: String): CapacityReview?
    fun save(item: CapacityReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityReviewStore(
    var items: MutableMap<String, CapacityReview>,
    var order: MutableList<String>,
) : CapacityReviewStore {
    override fun find(code: String): CapacityReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityReview) {
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

    override fun list(): MutableList<CapacityReview> {
        val out = mutableListOf<CapacityReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityReviewStore(): CapacityReviewStore {
    return MemoryCapacityReviewStore(mutableMapOf<String, CapacityReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityReviewStore(
    var inner: CapacityReviewStore,
    var cache: TtlCache<CapacityReview>,
    var hits: Int,
    var misses: Int,
) : CapacityReviewStore {
    override fun find(code: String): CapacityReview? {
        val hit = (cache.get(code) as CapacityReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityReview> = inner.list()
}

fun newCachedCapacityReviewStore(inner: CapacityReviewStore, ttl: Int, clock: Clock): CapacityReviewStore {
    return CachedCapacityReviewStore(
        inner = inner,
        cache = newTtlCache<CapacityReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity reviews. Empty fields match all. */
data class CapacityReviewQuery(
    var depot: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityReview): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityReview>): MutableList<CapacityReview> {
        val matched = filter(items) { candidate: CapacityReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityReviews(store: CapacityReviewStore, query: CapacityReviewQuery): MutableList<CapacityReview> {
    return query.apply(store.list())
}
