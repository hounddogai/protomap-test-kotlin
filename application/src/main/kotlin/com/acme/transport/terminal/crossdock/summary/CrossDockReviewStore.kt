package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock reviews by code. */
interface CrossDockReviewStore {
    fun find(code: String): CrossDockReview?
    fun save(item: CrossDockReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockReviewStore(
    var items: MutableMap<String, CrossDockReview>,
    var order: MutableList<String>,
) : CrossDockReviewStore {
    override fun find(code: String): CrossDockReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockReview) {
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

    override fun list(): MutableList<CrossDockReview> {
        val out = mutableListOf<CrossDockReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockReviewStore(): CrossDockReviewStore {
    return MemoryCrossDockReviewStore(mutableMapOf<String, CrossDockReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockReviewStore(
    var inner: CrossDockReviewStore,
    var cache: TtlCache<CrossDockReview>,
    var hits: Int,
    var misses: Int,
) : CrossDockReviewStore {
    override fun find(code: String): CrossDockReview? {
        val hit = (cache.get(code) as CrossDockReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockReview> = inner.list()
}

fun newCachedCrossDockReviewStore(inner: CrossDockReviewStore, ttl: Int, clock: Clock): CrossDockReviewStore {
    return CachedCrossDockReviewStore(
        inner = inner,
        cache = newTtlCache<CrossDockReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock reviews. Empty fields match all. */
data class CrossDockReviewQuery(
    var unit: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockReview): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CrossDockReview>): MutableList<CrossDockReview> {
        val matched = filter(items) { candidate: CrossDockReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockReviews(store: CrossDockReviewStore, query: CrossDockReviewQuery): MutableList<CrossDockReview> {
    return query.apply(store.list())
}
