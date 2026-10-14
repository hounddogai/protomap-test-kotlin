package com.acme.transport.network.zone.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps seasonal zone reviews by code. */
interface SeasonalZoneReviewStore {
    fun find(code: String): SeasonalZoneReview?
    fun save(item: SeasonalZoneReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalZoneReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalZoneReviewStore(
    var items: MutableMap<String, SeasonalZoneReview>,
    var order: MutableList<String>,
) : SeasonalZoneReviewStore {
    override fun find(code: String): SeasonalZoneReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalZoneReview) {
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

    override fun list(): MutableList<SeasonalZoneReview> {
        val out = mutableListOf<SeasonalZoneReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalZoneReviewStore(): SeasonalZoneReviewStore {
    return MemorySeasonalZoneReviewStore(mutableMapOf<String, SeasonalZoneReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalZoneReviewStore(
    var inner: SeasonalZoneReviewStore,
    var cache: TtlCache<SeasonalZoneReview>,
    var hits: Int,
    var misses: Int,
) : SeasonalZoneReviewStore {
    override fun find(code: String): SeasonalZoneReview? {
        val hit = (cache.get(code) as SeasonalZoneReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalZoneReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalZoneReview> = inner.list()
}

fun newCachedSeasonalZoneReviewStore(inner: SeasonalZoneReviewStore, ttl: Int, clock: Clock): SeasonalZoneReviewStore {
    return CachedSeasonalZoneReviewStore(
        inner = inner,
        cache = newTtlCache<SeasonalZoneReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal zone reviews. Empty fields match all. */
data class SeasonalZoneReviewQuery(
    var region: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalZoneReview): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalZoneReview>): MutableList<SeasonalZoneReview> {
        val matched = filter(items) { candidate: SeasonalZoneReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalZoneReviews(store: SeasonalZoneReviewStore, query: SeasonalZoneReviewQuery): MutableList<SeasonalZoneReview> {
    return query.apply(store.list())
}
