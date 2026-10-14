package com.acme.transport.customs.broker.review

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal broker reviews by code. */
interface SeasonalBrokerReviewStore {
    fun find(code: String): SeasonalBrokerReview?
    fun save(item: SeasonalBrokerReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalBrokerReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalBrokerReviewStore(
    var items: MutableMap<String, SeasonalBrokerReview>,
    var order: MutableList<String>,
) : SeasonalBrokerReviewStore {
    override fun find(code: String): SeasonalBrokerReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalBrokerReview) {
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

    override fun list(): MutableList<SeasonalBrokerReview> {
        val out = mutableListOf<SeasonalBrokerReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalBrokerReviewStore(): SeasonalBrokerReviewStore {
    return MemorySeasonalBrokerReviewStore(
        mutableMapOf<String, SeasonalBrokerReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalBrokerReviewStore(
    var inner: SeasonalBrokerReviewStore,
    var cache: TtlCache<SeasonalBrokerReview>,
    var hits: Int,
    var misses: Int,
) : SeasonalBrokerReviewStore {
    override fun find(code: String): SeasonalBrokerReview? {
        val hit = (cache.get(code) as SeasonalBrokerReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalBrokerReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalBrokerReview> = inner.list()
}

fun newCachedSeasonalBrokerReviewStore(inner: SeasonalBrokerReviewStore, ttl: Int, clock: Clock): SeasonalBrokerReviewStore {
    return CachedSeasonalBrokerReviewStore(
        inner = inner,
        cache = newTtlCache<SeasonalBrokerReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal broker reviews. Empty fields match all. */
data class SeasonalBrokerReviewQuery(
    var source: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalBrokerReview): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<SeasonalBrokerReview>): MutableList<SeasonalBrokerReview> {
        val matched = filter(items) { candidate: SeasonalBrokerReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalBrokerReviews(store: SeasonalBrokerReviewStore, query: SeasonalBrokerReviewQuery): MutableList<SeasonalBrokerReview> {
    return query.apply(store.list())
}
