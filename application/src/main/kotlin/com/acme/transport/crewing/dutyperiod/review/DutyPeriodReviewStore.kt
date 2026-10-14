package com.acme.transport.crewing.dutyperiod.review

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period reviews by code. */
interface DutyPeriodReviewStore {
    fun find(code: String): DutyPeriodReview?
    fun save(item: DutyPeriodReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodReviewStore(
    var items: MutableMap<String, DutyPeriodReview>,
    var order: MutableList<String>,
) : DutyPeriodReviewStore {
    override fun find(code: String): DutyPeriodReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodReview) {
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

    override fun list(): MutableList<DutyPeriodReview> {
        val out = mutableListOf<DutyPeriodReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodReviewStore(): DutyPeriodReviewStore {
    return MemoryDutyPeriodReviewStore(mutableMapOf<String, DutyPeriodReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodReviewStore(
    var inner: DutyPeriodReviewStore,
    var cache: TtlCache<DutyPeriodReview>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodReviewStore {
    override fun find(code: String): DutyPeriodReview? {
        val hit = (cache.get(code) as DutyPeriodReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodReview> = inner.list()
}

fun newCachedDutyPeriodReviewStore(inner: DutyPeriodReviewStore, ttl: Int, clock: Clock): DutyPeriodReviewStore {
    return CachedDutyPeriodReviewStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period reviews. Empty fields match all. */
data class DutyPeriodReviewQuery(
    var country: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodReview): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodReview>): MutableList<DutyPeriodReview> {
        val matched = filter(items) { candidate: DutyPeriodReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodReviews(store: DutyPeriodReviewStore, query: DutyPeriodReviewQuery): MutableList<DutyPeriodReview> {
    return query.apply(store.list())
}
