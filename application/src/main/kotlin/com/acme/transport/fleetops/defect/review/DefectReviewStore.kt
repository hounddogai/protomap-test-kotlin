package com.acme.transport.fleetops.defect.review

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect reviews by code. */
interface DefectReviewStore {
    fun find(code: String): DefectReview?
    fun save(item: DefectReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectReviewStore(
    var items: MutableMap<String, DefectReview>,
    var order: MutableList<String>,
) : DefectReviewStore {
    override fun find(code: String): DefectReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectReview) {
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

    override fun list(): MutableList<DefectReview> {
        val out = mutableListOf<DefectReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectReviewStore(): DefectReviewStore {
    return MemoryDefectReviewStore(mutableMapOf<String, DefectReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectReviewStore(
    var inner: DefectReviewStore,
    var cache: TtlCache<DefectReview>,
    var hits: Int,
    var misses: Int,
) : DefectReviewStore {
    override fun find(code: String): DefectReview? {
        val hit = (cache.get(code) as DefectReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectReview> = inner.list()
}

fun newCachedDefectReviewStore(inner: DefectReviewStore, ttl: Int, clock: Clock): DefectReviewStore {
    return CachedDefectReviewStore(
        inner = inner,
        cache = newTtlCache<DefectReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect reviews. Empty fields match all. */
data class DefectReviewQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectReview>): MutableList<DefectReview> {
        val matched = filter(items) { candidate: DefectReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectReviews(store: DefectReviewStore, query: DefectReviewQuery): MutableList<DefectReview> {
    return query.apply(store.list())
}
