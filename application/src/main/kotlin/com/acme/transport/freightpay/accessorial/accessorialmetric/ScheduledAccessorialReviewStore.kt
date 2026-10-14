package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled accessorial reviews by code. */
interface ScheduledAccessorialReviewStore {
    fun find(code: String): ScheduledAccessorialReview?
    fun save(item: ScheduledAccessorialReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledAccessorialReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledAccessorialReviewStore(
    var items: MutableMap<String, ScheduledAccessorialReview>,
    var order: MutableList<String>,
) : ScheduledAccessorialReviewStore {
    override fun find(code: String): ScheduledAccessorialReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledAccessorialReview) {
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

    override fun list(): MutableList<ScheduledAccessorialReview> {
        val out = mutableListOf<ScheduledAccessorialReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledAccessorialReviewStore(): ScheduledAccessorialReviewStore {
    return MemoryScheduledAccessorialReviewStore(
        mutableMapOf<String, ScheduledAccessorialReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledAccessorialReviewStore(
    var inner: ScheduledAccessorialReviewStore,
    var cache: TtlCache<ScheduledAccessorialReview>,
    var hits: Int,
    var misses: Int,
) : ScheduledAccessorialReviewStore {
    override fun find(code: String): ScheduledAccessorialReview? {
        val hit = (cache.get(code) as ScheduledAccessorialReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledAccessorialReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledAccessorialReview> = inner.list()
}

fun newCachedScheduledAccessorialReviewStore(inner: ScheduledAccessorialReviewStore, ttl: Int, clock: Clock): ScheduledAccessorialReviewStore {
    return CachedScheduledAccessorialReviewStore(
        inner = inner,
        cache = newTtlCache<ScheduledAccessorialReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled accessorial reviews. Empty fields match all. */
data class ScheduledAccessorialReviewQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledAccessorialReview): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<ScheduledAccessorialReview>): MutableList<ScheduledAccessorialReview> {
        val matched = filter(items) { candidate: ScheduledAccessorialReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledAccessorialReviews(store: ScheduledAccessorialReviewStore, query: ScheduledAccessorialReviewQuery): MutableList<ScheduledAccessorialReview> {
    return query.apply(store.list())
}
