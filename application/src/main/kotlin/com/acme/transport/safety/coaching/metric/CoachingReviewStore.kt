package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps coaching reviews by code. */
interface CoachingReviewStore {
    fun find(code: String): CoachingReview?
    fun save(item: CoachingReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<CoachingReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCoachingReviewStore(
    var items: MutableMap<String, CoachingReview>,
    var order: MutableList<String>,
) : CoachingReviewStore {
    override fun find(code: String): CoachingReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CoachingReview) {
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

    override fun list(): MutableList<CoachingReview> {
        val out = mutableListOf<CoachingReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCoachingReviewStore(): CoachingReviewStore {
    return MemoryCoachingReviewStore(mutableMapOf<String, CoachingReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCoachingReviewStore(
    var inner: CoachingReviewStore,
    var cache: TtlCache<CoachingReview>,
    var hits: Int,
    var misses: Int,
) : CoachingReviewStore {
    override fun find(code: String): CoachingReview? {
        val hit = (cache.get(code) as CoachingReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CoachingReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CoachingReview> = inner.list()
}

fun newCachedCoachingReviewStore(inner: CoachingReviewStore, ttl: Int, clock: Clock): CoachingReviewStore {
    return CachedCoachingReviewStore(
        inner = inner,
        cache = newTtlCache<CoachingReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects coaching reviews. Empty fields match all. */
data class CoachingReviewQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CoachingReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CoachingReview>): MutableList<CoachingReview> {
        val matched = filter(items) { candidate: CoachingReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCoachingReviews(store: CoachingReviewStore, query: CoachingReviewQuery): MutableList<CoachingReview> {
    return query.apply(store.list())
}
