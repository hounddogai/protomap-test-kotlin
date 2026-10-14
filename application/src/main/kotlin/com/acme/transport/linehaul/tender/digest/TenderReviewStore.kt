package com.acme.transport.linehaul.tender.digest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender reviews by code. */
interface TenderReviewStore {
    fun find(code: String): TenderReview?
    fun save(item: TenderReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderReviewStore(
    var items: MutableMap<String, TenderReview>,
    var order: MutableList<String>,
) : TenderReviewStore {
    override fun find(code: String): TenderReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderReview) {
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

    override fun list(): MutableList<TenderReview> {
        val out = mutableListOf<TenderReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderReviewStore(): TenderReviewStore {
    return MemoryTenderReviewStore(mutableMapOf<String, TenderReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderReviewStore(
    var inner: TenderReviewStore,
    var cache: TtlCache<TenderReview>,
    var hits: Int,
    var misses: Int,
) : TenderReviewStore {
    override fun find(code: String): TenderReview? {
        val hit = (cache.get(code) as TenderReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderReview> = inner.list()
}

fun newCachedTenderReviewStore(inner: TenderReviewStore, ttl: Int, clock: Clock): TenderReviewStore {
    return CachedTenderReviewStore(
        inner = inner,
        cache = newTtlCache<TenderReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender reviews. Empty fields match all. */
data class TenderReviewQuery(
    var segment: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderReview): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<TenderReview>): MutableList<TenderReview> {
        val matched = filter(items) { candidate: TenderReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderReviews(store: TenderReviewStore, query: TenderReviewQuery): MutableList<TenderReview> {
    return query.apply(store.list())
}
