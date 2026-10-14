package com.acme.transport.customs.inspection.review

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive inspection reviews by code. */
interface AdaptiveInspectionReviewStore {
    fun find(code: String): AdaptiveInspectionReview?
    fun save(item: AdaptiveInspectionReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveInspectionReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveInspectionReviewStore(
    var items: MutableMap<String, AdaptiveInspectionReview>,
    var order: MutableList<String>,
) : AdaptiveInspectionReviewStore {
    override fun find(code: String): AdaptiveInspectionReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveInspectionReview) {
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

    override fun list(): MutableList<AdaptiveInspectionReview> {
        val out = mutableListOf<AdaptiveInspectionReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveInspectionReviewStore(): AdaptiveInspectionReviewStore {
    return MemoryAdaptiveInspectionReviewStore(
        mutableMapOf<String, AdaptiveInspectionReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveInspectionReviewStore(
    var inner: AdaptiveInspectionReviewStore,
    var cache: TtlCache<AdaptiveInspectionReview>,
    var hits: Int,
    var misses: Int,
) : AdaptiveInspectionReviewStore {
    override fun find(code: String): AdaptiveInspectionReview? {
        val hit = (cache.get(code) as AdaptiveInspectionReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveInspectionReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveInspectionReview> = inner.list()
}

fun newCachedAdaptiveInspectionReviewStore(inner: AdaptiveInspectionReviewStore, ttl: Int, clock: Clock): AdaptiveInspectionReviewStore {
    return CachedAdaptiveInspectionReviewStore(
        inner = inner,
        cache = newTtlCache<AdaptiveInspectionReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive inspection reviews. Empty fields match all. */
data class AdaptiveInspectionReviewQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveInspectionReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveInspectionReview>): MutableList<AdaptiveInspectionReview> {
        val matched = filter(items) { candidate: AdaptiveInspectionReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveInspectionReviews(store: AdaptiveInspectionReviewStore, query: AdaptiveInspectionReviewQuery): MutableList<AdaptiveInspectionReview> {
    return query.apply(store.list())
}
