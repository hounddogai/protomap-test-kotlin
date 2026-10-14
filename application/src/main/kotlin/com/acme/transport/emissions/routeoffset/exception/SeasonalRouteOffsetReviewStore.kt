package com.acme.transport.emissions.routeoffset.exception

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal route offset reviews by code. */
interface SeasonalRouteOffsetReviewStore {
    fun find(code: String): SeasonalRouteOffsetReview?
    fun save(item: SeasonalRouteOffsetReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRouteOffsetReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRouteOffsetReviewStore(
    var items: MutableMap<String, SeasonalRouteOffsetReview>,
    var order: MutableList<String>,
) : SeasonalRouteOffsetReviewStore {
    override fun find(code: String): SeasonalRouteOffsetReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRouteOffsetReview) {
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

    override fun list(): MutableList<SeasonalRouteOffsetReview> {
        val out = mutableListOf<SeasonalRouteOffsetReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRouteOffsetReviewStore(): SeasonalRouteOffsetReviewStore {
    return MemorySeasonalRouteOffsetReviewStore(
        mutableMapOf<String, SeasonalRouteOffsetReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRouteOffsetReviewStore(
    var inner: SeasonalRouteOffsetReviewStore,
    var cache: TtlCache<SeasonalRouteOffsetReview>,
    var hits: Int,
    var misses: Int,
) : SeasonalRouteOffsetReviewStore {
    override fun find(code: String): SeasonalRouteOffsetReview? {
        val hit = (cache.get(code) as SeasonalRouteOffsetReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRouteOffsetReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRouteOffsetReview> = inner.list()
}

fun newCachedSeasonalRouteOffsetReviewStore(inner: SeasonalRouteOffsetReviewStore, ttl: Int, clock: Clock): SeasonalRouteOffsetReviewStore {
    return CachedSeasonalRouteOffsetReviewStore(
        inner = inner,
        cache = newTtlCache<SeasonalRouteOffsetReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal route offset reviews. Empty fields match all. */
data class SeasonalRouteOffsetReviewQuery(
    var label: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRouteOffsetReview): Boolean {
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

    fun apply(items: MutableList<SeasonalRouteOffsetReview>): MutableList<SeasonalRouteOffsetReview> {
        val matched = filter(items) { candidate: SeasonalRouteOffsetReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRouteOffsetReviews(store: SeasonalRouteOffsetReviewStore, query: SeasonalRouteOffsetReviewQuery): MutableList<SeasonalRouteOffsetReview> {
    return query.apply(store.list())
}
