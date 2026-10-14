package com.acme.transport.crewing.roster.rostersummary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster reviews by code. */
interface RosterReviewStore {
    fun find(code: String): RosterReview?
    fun save(item: RosterReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterReviewStore(
    var items: MutableMap<String, RosterReview>,
    var order: MutableList<String>,
) : RosterReviewStore {
    override fun find(code: String): RosterReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterReview) {
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

    override fun list(): MutableList<RosterReview> {
        val out = mutableListOf<RosterReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterReviewStore(): RosterReviewStore {
    return MemoryRosterReviewStore(mutableMapOf<String, RosterReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterReviewStore(
    var inner: RosterReviewStore,
    var cache: TtlCache<RosterReview>,
    var hits: Int,
    var misses: Int,
) : RosterReviewStore {
    override fun find(code: String): RosterReview? {
        val hit = (cache.get(code) as RosterReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterReview> = inner.list()
}

fun newCachedRosterReviewStore(inner: RosterReviewStore, ttl: Int, clock: Clock): RosterReviewStore {
    return CachedRosterReviewStore(
        inner = inner,
        cache = newTtlCache<RosterReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster reviews. Empty fields match all. */
data class RosterReviewQuery(
    var country: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterReview): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterReview>): MutableList<RosterReview> {
        val matched = filter(items) { candidate: RosterReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterReviews(store: RosterReviewStore, query: RosterReviewQuery): MutableList<RosterReview> {
    return query.apply(store.list())
}
