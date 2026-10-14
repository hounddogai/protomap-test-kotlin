package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization reviews by code. */
interface UtilizationReviewStore {
    fun find(code: String): UtilizationReview?
    fun save(item: UtilizationReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationReviewStore(
    var items: MutableMap<String, UtilizationReview>,
    var order: MutableList<String>,
) : UtilizationReviewStore {
    override fun find(code: String): UtilizationReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationReview) {
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

    override fun list(): MutableList<UtilizationReview> {
        val out = mutableListOf<UtilizationReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationReviewStore(): UtilizationReviewStore {
    return MemoryUtilizationReviewStore(mutableMapOf<String, UtilizationReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationReviewStore(
    var inner: UtilizationReviewStore,
    var cache: TtlCache<UtilizationReview>,
    var hits: Int,
    var misses: Int,
) : UtilizationReviewStore {
    override fun find(code: String): UtilizationReview? {
        val hit = (cache.get(code) as UtilizationReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationReview> = inner.list()
}

fun newCachedUtilizationReviewStore(inner: UtilizationReviewStore, ttl: Int, clock: Clock): UtilizationReviewStore {
    return CachedUtilizationReviewStore(
        inner = inner,
        cache = newTtlCache<UtilizationReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization reviews. Empty fields match all. */
data class UtilizationReviewQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationReview>): MutableList<UtilizationReview> {
        val matched = filter(items) { candidate: UtilizationReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationReviews(store: UtilizationReviewStore, query: UtilizationReviewQuery): MutableList<UtilizationReview> {
    return query.apply(store.list())
}
