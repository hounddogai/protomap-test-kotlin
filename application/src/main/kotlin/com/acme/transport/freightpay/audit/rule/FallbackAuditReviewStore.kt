package com.acme.transport.freightpay.audit.rule

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback audit reviews by code. */
interface FallbackAuditReviewStore {
    fun find(code: String): FallbackAuditReview?
    fun save(item: FallbackAuditReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackAuditReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackAuditReviewStore(
    var items: MutableMap<String, FallbackAuditReview>,
    var order: MutableList<String>,
) : FallbackAuditReviewStore {
    override fun find(code: String): FallbackAuditReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackAuditReview) {
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

    override fun list(): MutableList<FallbackAuditReview> {
        val out = mutableListOf<FallbackAuditReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackAuditReviewStore(): FallbackAuditReviewStore {
    return MemoryFallbackAuditReviewStore(mutableMapOf<String, FallbackAuditReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackAuditReviewStore(
    var inner: FallbackAuditReviewStore,
    var cache: TtlCache<FallbackAuditReview>,
    var hits: Int,
    var misses: Int,
) : FallbackAuditReviewStore {
    override fun find(code: String): FallbackAuditReview? {
        val hit = (cache.get(code) as FallbackAuditReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackAuditReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackAuditReview> = inner.list()
}

fun newCachedFallbackAuditReviewStore(inner: FallbackAuditReviewStore, ttl: Int, clock: Clock): FallbackAuditReviewStore {
    return CachedFallbackAuditReviewStore(
        inner = inner,
        cache = newTtlCache<FallbackAuditReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback audit reviews. Empty fields match all. */
data class FallbackAuditReviewQuery(
    var title: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackAuditReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackAuditReview>): MutableList<FallbackAuditReview> {
        val matched = filter(items) { candidate: FallbackAuditReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackAuditReviews(store: FallbackAuditReviewStore, query: FallbackAuditReviewQuery): MutableList<FallbackAuditReview> {
    return query.apply(store.list())
}
