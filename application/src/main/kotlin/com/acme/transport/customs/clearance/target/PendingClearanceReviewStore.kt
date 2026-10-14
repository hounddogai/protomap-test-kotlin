package com.acme.transport.customs.clearance.target

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending clearance reviews by code. */
interface PendingClearanceReviewStore {
    fun find(code: String): PendingClearanceReview?
    fun save(item: PendingClearanceReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingClearanceReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingClearanceReviewStore(
    var items: MutableMap<String, PendingClearanceReview>,
    var order: MutableList<String>,
) : PendingClearanceReviewStore {
    override fun find(code: String): PendingClearanceReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingClearanceReview) {
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

    override fun list(): MutableList<PendingClearanceReview> {
        val out = mutableListOf<PendingClearanceReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingClearanceReviewStore(): PendingClearanceReviewStore {
    return MemoryPendingClearanceReviewStore(
        mutableMapOf<String, PendingClearanceReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingClearanceReviewStore(
    var inner: PendingClearanceReviewStore,
    var cache: TtlCache<PendingClearanceReview>,
    var hits: Int,
    var misses: Int,
) : PendingClearanceReviewStore {
    override fun find(code: String): PendingClearanceReview? {
        val hit = (cache.get(code) as PendingClearanceReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingClearanceReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingClearanceReview> = inner.list()
}

fun newCachedPendingClearanceReviewStore(inner: PendingClearanceReviewStore, ttl: Int, clock: Clock): PendingClearanceReviewStore {
    return CachedPendingClearanceReviewStore(
        inner = inner,
        cache = newTtlCache<PendingClearanceReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending clearance reviews. Empty fields match all. */
data class PendingClearanceReviewQuery(
    var title: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingClearanceReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingClearanceReview>): MutableList<PendingClearanceReview> {
        val matched = filter(items) { candidate: PendingClearanceReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingClearanceReviews(store: PendingClearanceReviewStore, query: PendingClearanceReviewQuery): MutableList<PendingClearanceReview> {
    return query.apply(store.list())
}
