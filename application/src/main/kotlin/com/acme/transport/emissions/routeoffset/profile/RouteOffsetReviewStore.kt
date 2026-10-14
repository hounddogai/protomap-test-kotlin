package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset reviews by code. */
interface RouteOffsetReviewStore {
    fun find(code: String): RouteOffsetReview?
    fun save(item: RouteOffsetReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetReviewStore(
    var items: MutableMap<String, RouteOffsetReview>,
    var order: MutableList<String>,
) : RouteOffsetReviewStore {
    override fun find(code: String): RouteOffsetReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetReview) {
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

    override fun list(): MutableList<RouteOffsetReview> {
        val out = mutableListOf<RouteOffsetReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetReviewStore(): RouteOffsetReviewStore {
    return MemoryRouteOffsetReviewStore(mutableMapOf<String, RouteOffsetReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetReviewStore(
    var inner: RouteOffsetReviewStore,
    var cache: TtlCache<RouteOffsetReview>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetReviewStore {
    override fun find(code: String): RouteOffsetReview? {
        val hit = (cache.get(code) as RouteOffsetReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetReview> = inner.list()
}

fun newCachedRouteOffsetReviewStore(inner: RouteOffsetReviewStore, ttl: Int, clock: Clock): RouteOffsetReviewStore {
    return CachedRouteOffsetReviewStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset reviews. Empty fields match all. */
data class RouteOffsetReviewQuery(
    var country: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetReview): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetReview>): MutableList<RouteOffsetReview> {
        val matched = filter(items) { candidate: RouteOffsetReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetReviews(store: RouteOffsetReviewStore, query: RouteOffsetReviewQuery): MutableList<RouteOffsetReview> {
    return query.apply(store.list())
}
