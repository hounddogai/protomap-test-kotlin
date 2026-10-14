package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps attempt reviews by code. */
interface AttemptReviewStore {
    fun find(code: String): AttemptReview?
    fun save(item: AttemptReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<AttemptReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAttemptReviewStore(
    var items: MutableMap<String, AttemptReview>,
    var order: MutableList<String>,
) : AttemptReviewStore {
    override fun find(code: String): AttemptReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AttemptReview) {
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

    override fun list(): MutableList<AttemptReview> {
        val out = mutableListOf<AttemptReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAttemptReviewStore(): AttemptReviewStore {
    return MemoryAttemptReviewStore(mutableMapOf<String, AttemptReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAttemptReviewStore(
    var inner: AttemptReviewStore,
    var cache: TtlCache<AttemptReview>,
    var hits: Int,
    var misses: Int,
) : AttemptReviewStore {
    override fun find(code: String): AttemptReview? {
        val hit = (cache.get(code) as AttemptReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AttemptReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AttemptReview> = inner.list()
}

fun newCachedAttemptReviewStore(inner: AttemptReviewStore, ttl: Int, clock: Clock): AttemptReviewStore {
    return CachedAttemptReviewStore(
        inner = inner,
        cache = newTtlCache<AttemptReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects attempt reviews. Empty fields match all. */
data class AttemptReviewQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AttemptReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AttemptReview>): MutableList<AttemptReview> {
        val matched = filter(items) { candidate: AttemptReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAttemptReviews(store: AttemptReviewStore, query: AttemptReviewQuery): MutableList<AttemptReview> {
    return query.apply(store.list())
}
