package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback fuel burn reviews by code. */
interface FallbackFuelBurnReviewStore {
    fun find(code: String): FallbackFuelBurnReview?
    fun save(item: FallbackFuelBurnReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackFuelBurnReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackFuelBurnReviewStore(
    var items: MutableMap<String, FallbackFuelBurnReview>,
    var order: MutableList<String>,
) : FallbackFuelBurnReviewStore {
    override fun find(code: String): FallbackFuelBurnReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackFuelBurnReview) {
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

    override fun list(): MutableList<FallbackFuelBurnReview> {
        val out = mutableListOf<FallbackFuelBurnReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackFuelBurnReviewStore(): FallbackFuelBurnReviewStore {
    return MemoryFallbackFuelBurnReviewStore(
        mutableMapOf<String, FallbackFuelBurnReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackFuelBurnReviewStore(
    var inner: FallbackFuelBurnReviewStore,
    var cache: TtlCache<FallbackFuelBurnReview>,
    var hits: Int,
    var misses: Int,
) : FallbackFuelBurnReviewStore {
    override fun find(code: String): FallbackFuelBurnReview? {
        val hit = (cache.get(code) as FallbackFuelBurnReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackFuelBurnReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackFuelBurnReview> = inner.list()
}

fun newCachedFallbackFuelBurnReviewStore(inner: FallbackFuelBurnReviewStore, ttl: Int, clock: Clock): FallbackFuelBurnReviewStore {
    return CachedFallbackFuelBurnReviewStore(
        inner = inner,
        cache = newTtlCache<FallbackFuelBurnReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback fuel burn reviews. Empty fields match all. */
data class FallbackFuelBurnReviewQuery(
    var channel: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackFuelBurnReview): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackFuelBurnReview>): MutableList<FallbackFuelBurnReview> {
        val matched = filter(items) { candidate: FallbackFuelBurnReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackFuelBurnReviews(store: FallbackFuelBurnReviewStore, query: FallbackFuelBurnReviewQuery): MutableList<FallbackFuelBurnReview> {
    return query.apply(store.list())
}
