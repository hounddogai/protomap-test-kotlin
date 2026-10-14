package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rest break reviews by code. */
interface RestBreakReviewStore {
    fun find(code: String): RestBreakReview?
    fun save(item: RestBreakReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<RestBreakReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRestBreakReviewStore(
    var items: MutableMap<String, RestBreakReview>,
    var order: MutableList<String>,
) : RestBreakReviewStore {
    override fun find(code: String): RestBreakReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RestBreakReview) {
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

    override fun list(): MutableList<RestBreakReview> {
        val out = mutableListOf<RestBreakReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRestBreakReviewStore(): RestBreakReviewStore {
    return MemoryRestBreakReviewStore(mutableMapOf<String, RestBreakReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRestBreakReviewStore(
    var inner: RestBreakReviewStore,
    var cache: TtlCache<RestBreakReview>,
    var hits: Int,
    var misses: Int,
) : RestBreakReviewStore {
    override fun find(code: String): RestBreakReview? {
        val hit = (cache.get(code) as RestBreakReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RestBreakReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RestBreakReview> = inner.list()
}

fun newCachedRestBreakReviewStore(inner: RestBreakReviewStore, ttl: Int, clock: Clock): RestBreakReviewStore {
    return CachedRestBreakReviewStore(
        inner = inner,
        cache = newTtlCache<RestBreakReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rest break reviews. Empty fields match all. */
data class RestBreakReviewQuery(
    var title: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RestBreakReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RestBreakReview>): MutableList<RestBreakReview> {
        val matched = filter(items) { candidate: RestBreakReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRestBreakReviews(store: RestBreakReviewStore, query: RestBreakReviewQuery): MutableList<RestBreakReview> {
    return query.apply(store.list())
}
