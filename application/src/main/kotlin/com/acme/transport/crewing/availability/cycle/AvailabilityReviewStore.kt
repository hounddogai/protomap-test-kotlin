package com.acme.transport.crewing.availability.cycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps availability reviews by code. */
interface AvailabilityReviewStore {
    fun find(code: String): AvailabilityReview?
    fun save(item: AvailabilityReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<AvailabilityReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAvailabilityReviewStore(
    var items: MutableMap<String, AvailabilityReview>,
    var order: MutableList<String>,
) : AvailabilityReviewStore {
    override fun find(code: String): AvailabilityReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AvailabilityReview) {
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

    override fun list(): MutableList<AvailabilityReview> {
        val out = mutableListOf<AvailabilityReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAvailabilityReviewStore(): AvailabilityReviewStore {
    return MemoryAvailabilityReviewStore(mutableMapOf<String, AvailabilityReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAvailabilityReviewStore(
    var inner: AvailabilityReviewStore,
    var cache: TtlCache<AvailabilityReview>,
    var hits: Int,
    var misses: Int,
) : AvailabilityReviewStore {
    override fun find(code: String): AvailabilityReview? {
        val hit = (cache.get(code) as AvailabilityReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AvailabilityReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AvailabilityReview> = inner.list()
}

fun newCachedAvailabilityReviewStore(inner: AvailabilityReviewStore, ttl: Int, clock: Clock): AvailabilityReviewStore {
    return CachedAvailabilityReviewStore(
        inner = inner,
        cache = newTtlCache<AvailabilityReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects availability reviews. Empty fields match all. */
data class AvailabilityReviewQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AvailabilityReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AvailabilityReview>): MutableList<AvailabilityReview> {
        val matched = filter(items) { candidate: AvailabilityReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAvailabilityReviews(store: AvailabilityReviewStore, query: AvailabilityReviewQuery): MutableList<AvailabilityReview> {
    return query.apply(store.list())
}
