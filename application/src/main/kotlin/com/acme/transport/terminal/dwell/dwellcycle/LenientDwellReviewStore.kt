package com.acme.transport.terminal.dwell.dwellcycle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps lenient dwell reviews by code. */
interface LenientDwellReviewStore {
    fun find(code: String): LenientDwellReview?
    fun save(item: LenientDwellReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDwellReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDwellReviewStore(
    var items: MutableMap<String, LenientDwellReview>,
    var order: MutableList<String>,
) : LenientDwellReviewStore {
    override fun find(code: String): LenientDwellReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDwellReview) {
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

    override fun list(): MutableList<LenientDwellReview> {
        val out = mutableListOf<LenientDwellReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDwellReviewStore(): LenientDwellReviewStore {
    return MemoryLenientDwellReviewStore(mutableMapOf<String, LenientDwellReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDwellReviewStore(
    var inner: LenientDwellReviewStore,
    var cache: TtlCache<LenientDwellReview>,
    var hits: Int,
    var misses: Int,
) : LenientDwellReviewStore {
    override fun find(code: String): LenientDwellReview? {
        val hit = (cache.get(code) as LenientDwellReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDwellReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDwellReview> = inner.list()
}

fun newCachedLenientDwellReviewStore(inner: LenientDwellReviewStore, ttl: Int, clock: Clock): LenientDwellReviewStore {
    return CachedLenientDwellReviewStore(
        inner = inner,
        cache = newTtlCache<LenientDwellReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient dwell reviews. Empty fields match all. */
data class LenientDwellReviewQuery(
    var depot: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDwellReview): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDwellReview>): MutableList<LenientDwellReview> {
        val matched = filter(items) { candidate: LenientDwellReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDwellReviews(store: LenientDwellReviewStore, query: LenientDwellReviewQuery): MutableList<LenientDwellReview> {
    return query.apply(store.list())
}
