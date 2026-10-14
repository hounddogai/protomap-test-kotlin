package com.acme.transport.customs.clearance.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance reviews by code. */
interface ClearanceReviewStore {
    fun find(code: String): ClearanceReview?
    fun save(item: ClearanceReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceReviewStore(
    var items: MutableMap<String, ClearanceReview>,
    var order: MutableList<String>,
) : ClearanceReviewStore {
    override fun find(code: String): ClearanceReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceReview) {
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

    override fun list(): MutableList<ClearanceReview> {
        val out = mutableListOf<ClearanceReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceReviewStore(): ClearanceReviewStore {
    return MemoryClearanceReviewStore(mutableMapOf<String, ClearanceReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceReviewStore(
    var inner: ClearanceReviewStore,
    var cache: TtlCache<ClearanceReview>,
    var hits: Int,
    var misses: Int,
) : ClearanceReviewStore {
    override fun find(code: String): ClearanceReview? {
        val hit = (cache.get(code) as ClearanceReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceReview> = inner.list()
}

fun newCachedClearanceReviewStore(inner: ClearanceReviewStore, ttl: Int, clock: Clock): ClearanceReviewStore {
    return CachedClearanceReviewStore(
        inner = inner,
        cache = newTtlCache<ClearanceReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance reviews. Empty fields match all. */
data class ClearanceReviewQuery(
    var source: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceReview): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceReview>): MutableList<ClearanceReview> {
        val matched = filter(items) { candidate: ClearanceReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceReviews(store: ClearanceReviewStore, query: ClearanceReviewQuery): MutableList<ClearanceReview> {
    return query.apply(store.list())
}
