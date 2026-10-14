package com.acme.transport.safety.scorecard.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted scorecard reviews by code. */
interface WeightedScorecardReviewStore {
    fun find(code: String): WeightedScorecardReview?
    fun save(item: WeightedScorecardReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedScorecardReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedScorecardReviewStore(
    var items: MutableMap<String, WeightedScorecardReview>,
    var order: MutableList<String>,
) : WeightedScorecardReviewStore {
    override fun find(code: String): WeightedScorecardReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedScorecardReview) {
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

    override fun list(): MutableList<WeightedScorecardReview> {
        val out = mutableListOf<WeightedScorecardReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedScorecardReviewStore(): WeightedScorecardReviewStore {
    return MemoryWeightedScorecardReviewStore(
        mutableMapOf<String, WeightedScorecardReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedScorecardReviewStore(
    var inner: WeightedScorecardReviewStore,
    var cache: TtlCache<WeightedScorecardReview>,
    var hits: Int,
    var misses: Int,
) : WeightedScorecardReviewStore {
    override fun find(code: String): WeightedScorecardReview? {
        val hit = (cache.get(code) as WeightedScorecardReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedScorecardReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedScorecardReview> = inner.list()
}

fun newCachedWeightedScorecardReviewStore(inner: WeightedScorecardReviewStore, ttl: Int, clock: Clock): WeightedScorecardReviewStore {
    return CachedWeightedScorecardReviewStore(
        inner = inner,
        cache = newTtlCache<WeightedScorecardReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted scorecard reviews. Empty fields match all. */
data class WeightedScorecardReviewQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedScorecardReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedScorecardReview>): MutableList<WeightedScorecardReview> {
        val matched = filter(items) { candidate: WeightedScorecardReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedScorecardReviews(store: WeightedScorecardReviewStore, query: WeightedScorecardReviewQuery): MutableList<WeightedScorecardReview> {
    return query.apply(store.list())
}
