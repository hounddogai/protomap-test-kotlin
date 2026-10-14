package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling inspection reviews by code. */
interface RollingInspectionReviewStore {
    fun find(code: String): RollingInspectionReview?
    fun save(item: RollingInspectionReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingInspectionReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingInspectionReviewStore(
    var items: MutableMap<String, RollingInspectionReview>,
    var order: MutableList<String>,
) : RollingInspectionReviewStore {
    override fun find(code: String): RollingInspectionReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingInspectionReview) {
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

    override fun list(): MutableList<RollingInspectionReview> {
        val out = mutableListOf<RollingInspectionReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingInspectionReviewStore(): RollingInspectionReviewStore {
    return MemoryRollingInspectionReviewStore(
        mutableMapOf<String, RollingInspectionReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingInspectionReviewStore(
    var inner: RollingInspectionReviewStore,
    var cache: TtlCache<RollingInspectionReview>,
    var hits: Int,
    var misses: Int,
) : RollingInspectionReviewStore {
    override fun find(code: String): RollingInspectionReview? {
        val hit = (cache.get(code) as RollingInspectionReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingInspectionReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingInspectionReview> = inner.list()
}

fun newCachedRollingInspectionReviewStore(inner: RollingInspectionReviewStore, ttl: Int, clock: Clock): RollingInspectionReviewStore {
    return CachedRollingInspectionReviewStore(
        inner = inner,
        cache = newTtlCache<RollingInspectionReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling inspection reviews. Empty fields match all. */
data class RollingInspectionReviewQuery(
    var name: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingInspectionReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingInspectionReview>): MutableList<RollingInspectionReview> {
        val matched = filter(items) { candidate: RollingInspectionReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingInspectionReviews(store: RollingInspectionReviewStore, query: RollingInspectionReviewQuery): MutableList<RollingInspectionReview> {
    return query.apply(store.list())
}
