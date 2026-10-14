package com.acme.transport.fleetops.inspection.review

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection reviews by code. */
interface InspectionReviewStore {
    fun find(code: String): InspectionReview?
    fun save(item: InspectionReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionReviewStore(
    var items: MutableMap<String, InspectionReview>,
    var order: MutableList<String>,
) : InspectionReviewStore {
    override fun find(code: String): InspectionReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionReview) {
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

    override fun list(): MutableList<InspectionReview> {
        val out = mutableListOf<InspectionReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionReviewStore(): InspectionReviewStore {
    return MemoryInspectionReviewStore(mutableMapOf<String, InspectionReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionReviewStore(
    var inner: InspectionReviewStore,
    var cache: TtlCache<InspectionReview>,
    var hits: Int,
    var misses: Int,
) : InspectionReviewStore {
    override fun find(code: String): InspectionReview? {
        val hit = (cache.get(code) as InspectionReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionReview> = inner.list()
}

fun newCachedInspectionReviewStore(inner: InspectionReviewStore, ttl: Int, clock: Clock): InspectionReviewStore {
    return CachedInspectionReviewStore(
        inner = inner,
        cache = newTtlCache<InspectionReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection reviews. Empty fields match all. */
data class InspectionReviewQuery(
    var title: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionReview>): MutableList<InspectionReview> {
        val matched = filter(items) { candidate: InspectionReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionReviews(store: InspectionReviewStore, query: InspectionReviewQuery): MutableList<InspectionReview> {
    return query.apply(store.list())
}
