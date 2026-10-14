package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict utilization reviews by code. */
interface StrictUtilizationReviewStore {
    fun find(code: String): StrictUtilizationReview?
    fun save(item: StrictUtilizationReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictUtilizationReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictUtilizationReviewStore(
    var items: MutableMap<String, StrictUtilizationReview>,
    var order: MutableList<String>,
) : StrictUtilizationReviewStore {
    override fun find(code: String): StrictUtilizationReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictUtilizationReview) {
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

    override fun list(): MutableList<StrictUtilizationReview> {
        val out = mutableListOf<StrictUtilizationReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictUtilizationReviewStore(): StrictUtilizationReviewStore {
    return MemoryStrictUtilizationReviewStore(
        mutableMapOf<String, StrictUtilizationReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictUtilizationReviewStore(
    var inner: StrictUtilizationReviewStore,
    var cache: TtlCache<StrictUtilizationReview>,
    var hits: Int,
    var misses: Int,
) : StrictUtilizationReviewStore {
    override fun find(code: String): StrictUtilizationReview? {
        val hit = (cache.get(code) as StrictUtilizationReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictUtilizationReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictUtilizationReview> = inner.list()
}

fun newCachedStrictUtilizationReviewStore(inner: StrictUtilizationReviewStore, ttl: Int, clock: Clock): StrictUtilizationReviewStore {
    return CachedStrictUtilizationReviewStore(
        inner = inner,
        cache = newTtlCache<StrictUtilizationReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict utilization reviews. Empty fields match all. */
data class StrictUtilizationReviewQuery(
    var segment: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictUtilizationReview): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictUtilizationReview>): MutableList<StrictUtilizationReview> {
        val matched = filter(items) { candidate: StrictUtilizationReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictUtilizationReviews(store: StrictUtilizationReviewStore, query: StrictUtilizationReviewQuery): MutableList<StrictUtilizationReview> {
    return query.apply(store.list())
}
