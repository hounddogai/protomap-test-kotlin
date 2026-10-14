package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient rest break reviews by code. */
interface LenientRestBreakReviewStore {
    fun find(code: String): LenientRestBreakReview?
    fun save(item: LenientRestBreakReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientRestBreakReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientRestBreakReviewStore(
    var items: MutableMap<String, LenientRestBreakReview>,
    var order: MutableList<String>,
) : LenientRestBreakReviewStore {
    override fun find(code: String): LenientRestBreakReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientRestBreakReview) {
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

    override fun list(): MutableList<LenientRestBreakReview> {
        val out = mutableListOf<LenientRestBreakReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientRestBreakReviewStore(): LenientRestBreakReviewStore {
    return MemoryLenientRestBreakReviewStore(
        mutableMapOf<String, LenientRestBreakReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientRestBreakReviewStore(
    var inner: LenientRestBreakReviewStore,
    var cache: TtlCache<LenientRestBreakReview>,
    var hits: Int,
    var misses: Int,
) : LenientRestBreakReviewStore {
    override fun find(code: String): LenientRestBreakReview? {
        val hit = (cache.get(code) as LenientRestBreakReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientRestBreakReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientRestBreakReview> = inner.list()
}

fun newCachedLenientRestBreakReviewStore(inner: LenientRestBreakReviewStore, ttl: Int, clock: Clock): LenientRestBreakReviewStore {
    return CachedLenientRestBreakReviewStore(
        inner = inner,
        cache = newTtlCache<LenientRestBreakReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient rest break reviews. Empty fields match all. */
data class LenientRestBreakReviewQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientRestBreakReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientRestBreakReview>): MutableList<LenientRestBreakReview> {
        val matched = filter(items) { candidate: LenientRestBreakReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientRestBreakReviews(store: LenientRestBreakReviewStore, query: LenientRestBreakReviewQuery): MutableList<LenientRestBreakReview> {
    return query.apply(store.list())
}
