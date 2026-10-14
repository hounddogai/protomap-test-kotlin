package com.acme.transport.fleetops.defect.exception

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling defect reviews by code. */
interface RollingDefectReviewStore {
    fun find(code: String): RollingDefectReview?
    fun save(item: RollingDefectReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDefectReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDefectReviewStore(
    var items: MutableMap<String, RollingDefectReview>,
    var order: MutableList<String>,
) : RollingDefectReviewStore {
    override fun find(code: String): RollingDefectReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDefectReview) {
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

    override fun list(): MutableList<RollingDefectReview> {
        val out = mutableListOf<RollingDefectReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDefectReviewStore(): RollingDefectReviewStore {
    return MemoryRollingDefectReviewStore(mutableMapOf<String, RollingDefectReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDefectReviewStore(
    var inner: RollingDefectReviewStore,
    var cache: TtlCache<RollingDefectReview>,
    var hits: Int,
    var misses: Int,
) : RollingDefectReviewStore {
    override fun find(code: String): RollingDefectReview? {
        val hit = (cache.get(code) as RollingDefectReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDefectReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDefectReview> = inner.list()
}

fun newCachedRollingDefectReviewStore(inner: RollingDefectReviewStore, ttl: Int, clock: Clock): RollingDefectReviewStore {
    return CachedRollingDefectReviewStore(
        inner = inner,
        cache = newTtlCache<RollingDefectReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling defect reviews. Empty fields match all. */
data class RollingDefectReviewQuery(
    var title: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDefectReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDefectReview>): MutableList<RollingDefectReview> {
        val matched = filter(items) { candidate: RollingDefectReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDefectReviews(store: RollingDefectReviewStore, query: RollingDefectReviewQuery): MutableList<RollingDefectReview> {
    return query.apply(store.list())
}
