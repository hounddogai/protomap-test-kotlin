package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps primary trailer reviews by code. */
interface PrimaryTrailerReviewStore {
    fun find(code: String): PrimaryTrailerReview?
    fun save(item: PrimaryTrailerReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTrailerReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTrailerReviewStore(
    var items: MutableMap<String, PrimaryTrailerReview>,
    var order: MutableList<String>,
) : PrimaryTrailerReviewStore {
    override fun find(code: String): PrimaryTrailerReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTrailerReview) {
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

    override fun list(): MutableList<PrimaryTrailerReview> {
        val out = mutableListOf<PrimaryTrailerReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTrailerReviewStore(): PrimaryTrailerReviewStore {
    return MemoryPrimaryTrailerReviewStore(
        mutableMapOf<String, PrimaryTrailerReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTrailerReviewStore(
    var inner: PrimaryTrailerReviewStore,
    var cache: TtlCache<PrimaryTrailerReview>,
    var hits: Int,
    var misses: Int,
) : PrimaryTrailerReviewStore {
    override fun find(code: String): PrimaryTrailerReview? {
        val hit = (cache.get(code) as PrimaryTrailerReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTrailerReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTrailerReview> = inner.list()
}

fun newCachedPrimaryTrailerReviewStore(inner: PrimaryTrailerReviewStore, ttl: Int, clock: Clock): PrimaryTrailerReviewStore {
    return CachedPrimaryTrailerReviewStore(
        inner = inner,
        cache = newTtlCache<PrimaryTrailerReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary trailer reviews. Empty fields match all. */
data class PrimaryTrailerReviewQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTrailerReview): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTrailerReview>): MutableList<PrimaryTrailerReview> {
        val matched = filter(items) { candidate: PrimaryTrailerReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTrailerReviews(store: PrimaryTrailerReviewStore, query: PrimaryTrailerReviewQuery): MutableList<PrimaryTrailerReview> {
    return query.apply(store.list())
}
