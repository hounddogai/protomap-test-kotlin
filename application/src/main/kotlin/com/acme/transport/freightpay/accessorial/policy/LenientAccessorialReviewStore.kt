package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient accessorial reviews by code. */
interface LenientAccessorialReviewStore {
    fun find(code: String): LenientAccessorialReview?
    fun save(item: LenientAccessorialReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientAccessorialReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientAccessorialReviewStore(
    var items: MutableMap<String, LenientAccessorialReview>,
    var order: MutableList<String>,
) : LenientAccessorialReviewStore {
    override fun find(code: String): LenientAccessorialReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientAccessorialReview) {
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

    override fun list(): MutableList<LenientAccessorialReview> {
        val out = mutableListOf<LenientAccessorialReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientAccessorialReviewStore(): LenientAccessorialReviewStore {
    return MemoryLenientAccessorialReviewStore(
        mutableMapOf<String, LenientAccessorialReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientAccessorialReviewStore(
    var inner: LenientAccessorialReviewStore,
    var cache: TtlCache<LenientAccessorialReview>,
    var hits: Int,
    var misses: Int,
) : LenientAccessorialReviewStore {
    override fun find(code: String): LenientAccessorialReview? {
        val hit = (cache.get(code) as LenientAccessorialReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientAccessorialReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientAccessorialReview> = inner.list()
}

fun newCachedLenientAccessorialReviewStore(inner: LenientAccessorialReviewStore, ttl: Int, clock: Clock): LenientAccessorialReviewStore {
    return CachedLenientAccessorialReviewStore(
        inner = inner,
        cache = newTtlCache<LenientAccessorialReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient accessorial reviews. Empty fields match all. */
data class LenientAccessorialReviewQuery(
    var locale: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientAccessorialReview): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientAccessorialReview>): MutableList<LenientAccessorialReview> {
        val matched = filter(items) { candidate: LenientAccessorialReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientAccessorialReviews(store: LenientAccessorialReviewStore, query: LenientAccessorialReviewQuery): MutableList<LenientAccessorialReview> {
    return query.apply(store.list())
}
