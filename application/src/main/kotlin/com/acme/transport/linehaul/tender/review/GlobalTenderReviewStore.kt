package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps global tender reviews by code. */
interface GlobalTenderReviewStore {
    fun find(code: String): GlobalTenderReview?
    fun save(item: GlobalTenderReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalTenderReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalTenderReviewStore(
    var items: MutableMap<String, GlobalTenderReview>,
    var order: MutableList<String>,
) : GlobalTenderReviewStore {
    override fun find(code: String): GlobalTenderReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalTenderReview) {
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

    override fun list(): MutableList<GlobalTenderReview> {
        val out = mutableListOf<GlobalTenderReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalTenderReviewStore(): GlobalTenderReviewStore {
    return MemoryGlobalTenderReviewStore(mutableMapOf<String, GlobalTenderReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalTenderReviewStore(
    var inner: GlobalTenderReviewStore,
    var cache: TtlCache<GlobalTenderReview>,
    var hits: Int,
    var misses: Int,
) : GlobalTenderReviewStore {
    override fun find(code: String): GlobalTenderReview? {
        val hit = (cache.get(code) as GlobalTenderReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalTenderReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalTenderReview> = inner.list()
}

fun newCachedGlobalTenderReviewStore(inner: GlobalTenderReviewStore, ttl: Int, clock: Clock): GlobalTenderReviewStore {
    return CachedGlobalTenderReviewStore(
        inner = inner,
        cache = newTtlCache<GlobalTenderReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global tender reviews. Empty fields match all. */
data class GlobalTenderReviewQuery(
    var unit: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalTenderReview): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalTenderReview>): MutableList<GlobalTenderReview> {
        val matched = filter(items) { candidate: GlobalTenderReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalTenderReviews(store: GlobalTenderReviewStore, query: GlobalTenderReviewQuery): MutableList<GlobalTenderReview> {
    return query.apply(store.list())
}
