package com.acme.transport.linehaul.trailer.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer reviews by code. */
interface TrailerReviewStore {
    fun find(code: String): TrailerReview?
    fun save(item: TrailerReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerReviewStore(
    var items: MutableMap<String, TrailerReview>,
    var order: MutableList<String>,
) : TrailerReviewStore {
    override fun find(code: String): TrailerReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerReview) {
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

    override fun list(): MutableList<TrailerReview> {
        val out = mutableListOf<TrailerReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerReviewStore(): TrailerReviewStore {
    return MemoryTrailerReviewStore(mutableMapOf<String, TrailerReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerReviewStore(
    var inner: TrailerReviewStore,
    var cache: TtlCache<TrailerReview>,
    var hits: Int,
    var misses: Int,
) : TrailerReviewStore {
    override fun find(code: String): TrailerReview? {
        val hit = (cache.get(code) as TrailerReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerReview> = inner.list()
}

fun newCachedTrailerReviewStore(inner: TrailerReviewStore, ttl: Int, clock: Clock): TrailerReviewStore {
    return CachedTrailerReviewStore(
        inner = inner,
        cache = newTtlCache<TrailerReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer reviews. Empty fields match all. */
data class TrailerReviewQuery(
    var name: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerReview>): MutableList<TrailerReview> {
        val matched = filter(items) { candidate: TrailerReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerReviews(store: TrailerReviewStore, query: TrailerReviewQuery): MutableList<TrailerReview> {
    return query.apply(store.list())
}
