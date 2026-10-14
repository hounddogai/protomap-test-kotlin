package com.acme.transport.freightpay.audit.auditreview

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict audit reviews by code. */
interface StrictAuditReviewStore {
    fun find(code: String): StrictAuditReview?
    fun save(item: StrictAuditReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictAuditReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictAuditReviewStore(
    var items: MutableMap<String, StrictAuditReview>,
    var order: MutableList<String>,
) : StrictAuditReviewStore {
    override fun find(code: String): StrictAuditReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictAuditReview) {
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

    override fun list(): MutableList<StrictAuditReview> {
        val out = mutableListOf<StrictAuditReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictAuditReviewStore(): StrictAuditReviewStore {
    return MemoryStrictAuditReviewStore(mutableMapOf<String, StrictAuditReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictAuditReviewStore(
    var inner: StrictAuditReviewStore,
    var cache: TtlCache<StrictAuditReview>,
    var hits: Int,
    var misses: Int,
) : StrictAuditReviewStore {
    override fun find(code: String): StrictAuditReview? {
        val hit = (cache.get(code) as StrictAuditReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictAuditReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictAuditReview> = inner.list()
}

fun newCachedStrictAuditReviewStore(inner: StrictAuditReviewStore, ttl: Int, clock: Clock): StrictAuditReviewStore {
    return CachedStrictAuditReviewStore(
        inner = inner,
        cache = newTtlCache<StrictAuditReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict audit reviews. Empty fields match all. */
data class StrictAuditReviewQuery(
    var source: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictAuditReview): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictAuditReview>): MutableList<StrictAuditReview> {
        val matched = filter(items) { candidate: StrictAuditReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictAuditReviews(store: StrictAuditReviewStore, query: StrictAuditReviewQuery): MutableList<StrictAuditReview> {
    return query.apply(store.list())
}
