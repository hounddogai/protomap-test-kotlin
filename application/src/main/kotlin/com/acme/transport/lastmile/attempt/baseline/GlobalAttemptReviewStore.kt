package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global attempt reviews by code. */
interface GlobalAttemptReviewStore {
    fun find(code: String): GlobalAttemptReview?
    fun save(item: GlobalAttemptReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalAttemptReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalAttemptReviewStore(
    var items: MutableMap<String, GlobalAttemptReview>,
    var order: MutableList<String>,
) : GlobalAttemptReviewStore {
    override fun find(code: String): GlobalAttemptReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalAttemptReview) {
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

    override fun list(): MutableList<GlobalAttemptReview> {
        val out = mutableListOf<GlobalAttemptReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalAttemptReviewStore(): GlobalAttemptReviewStore {
    return MemoryGlobalAttemptReviewStore(mutableMapOf<String, GlobalAttemptReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalAttemptReviewStore(
    var inner: GlobalAttemptReviewStore,
    var cache: TtlCache<GlobalAttemptReview>,
    var hits: Int,
    var misses: Int,
) : GlobalAttemptReviewStore {
    override fun find(code: String): GlobalAttemptReview? {
        val hit = (cache.get(code) as GlobalAttemptReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalAttemptReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalAttemptReview> = inner.list()
}

fun newCachedGlobalAttemptReviewStore(inner: GlobalAttemptReviewStore, ttl: Int, clock: Clock): GlobalAttemptReviewStore {
    return CachedGlobalAttemptReviewStore(
        inner = inner,
        cache = newTtlCache<GlobalAttemptReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global attempt reviews. Empty fields match all. */
data class GlobalAttemptReviewQuery(
    var category: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalAttemptReview): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalAttemptReview>): MutableList<GlobalAttemptReview> {
        val matched = filter(items) { candidate: GlobalAttemptReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalAttemptReviews(store: GlobalAttemptReviewStore, query: GlobalAttemptReviewQuery): MutableList<GlobalAttemptReview> {
    return query.apply(store.list())
}
