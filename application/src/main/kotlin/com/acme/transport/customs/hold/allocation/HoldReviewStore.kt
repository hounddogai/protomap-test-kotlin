package com.acme.transport.customs.hold.allocation

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps hold reviews by code. */
interface HoldReviewStore {
    fun find(code: String): HoldReview?
    fun save(item: HoldReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<HoldReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHoldReviewStore(
    var items: MutableMap<String, HoldReview>,
    var order: MutableList<String>,
) : HoldReviewStore {
    override fun find(code: String): HoldReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HoldReview) {
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

    override fun list(): MutableList<HoldReview> {
        val out = mutableListOf<HoldReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHoldReviewStore(): HoldReviewStore {
    return MemoryHoldReviewStore(mutableMapOf<String, HoldReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHoldReviewStore(
    var inner: HoldReviewStore,
    var cache: TtlCache<HoldReview>,
    var hits: Int,
    var misses: Int,
) : HoldReviewStore {
    override fun find(code: String): HoldReview? {
        val hit = (cache.get(code) as HoldReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HoldReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HoldReview> = inner.list()
}

fun newCachedHoldReviewStore(inner: HoldReviewStore, ttl: Int, clock: Clock): HoldReviewStore {
    return CachedHoldReviewStore(
        inner = inner,
        cache = newTtlCache<HoldReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hold reviews. Empty fields match all. */
data class HoldReviewQuery(
    var title: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HoldReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HoldReview>): MutableList<HoldReview> {
        val matched = filter(items) { candidate: HoldReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHoldReviews(store: HoldReviewStore, query: HoldReviewQuery): MutableList<HoldReview> {
    return query.apply(store.list())
}
