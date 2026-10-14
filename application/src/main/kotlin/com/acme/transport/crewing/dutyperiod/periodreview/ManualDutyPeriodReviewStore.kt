package com.acme.transport.crewing.dutyperiod.periodreview

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual duty period reviews by code. */
interface ManualDutyPeriodReviewStore {
    fun find(code: String): ManualDutyPeriodReview?
    fun save(item: ManualDutyPeriodReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDutyPeriodReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDutyPeriodReviewStore(
    var items: MutableMap<String, ManualDutyPeriodReview>,
    var order: MutableList<String>,
) : ManualDutyPeriodReviewStore {
    override fun find(code: String): ManualDutyPeriodReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDutyPeriodReview) {
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

    override fun list(): MutableList<ManualDutyPeriodReview> {
        val out = mutableListOf<ManualDutyPeriodReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDutyPeriodReviewStore(): ManualDutyPeriodReviewStore {
    return MemoryManualDutyPeriodReviewStore(
        mutableMapOf<String, ManualDutyPeriodReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDutyPeriodReviewStore(
    var inner: ManualDutyPeriodReviewStore,
    var cache: TtlCache<ManualDutyPeriodReview>,
    var hits: Int,
    var misses: Int,
) : ManualDutyPeriodReviewStore {
    override fun find(code: String): ManualDutyPeriodReview? {
        val hit = (cache.get(code) as ManualDutyPeriodReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDutyPeriodReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDutyPeriodReview> = inner.list()
}

fun newCachedManualDutyPeriodReviewStore(inner: ManualDutyPeriodReviewStore, ttl: Int, clock: Clock): ManualDutyPeriodReviewStore {
    return CachedManualDutyPeriodReviewStore(
        inner = inner,
        cache = newTtlCache<ManualDutyPeriodReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual duty period reviews. Empty fields match all. */
data class ManualDutyPeriodReviewQuery(
    var unit: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDutyPeriodReview): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDutyPeriodReview>): MutableList<ManualDutyPeriodReview> {
        val matched = filter(items) { candidate: ManualDutyPeriodReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDutyPeriodReviews(store: ManualDutyPeriodReviewStore, query: ManualDutyPeriodReviewQuery): MutableList<ManualDutyPeriodReview> {
    return query.apply(store.list())
}
