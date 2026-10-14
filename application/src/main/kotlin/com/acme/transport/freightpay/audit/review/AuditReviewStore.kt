package com.acme.transport.freightpay.audit.review

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit reviews by code. */
interface AuditReviewStore {
    fun find(code: String): AuditReview?
    fun save(item: AuditReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditReviewStore(
    var items: MutableMap<String, AuditReview>,
    var order: MutableList<String>,
) : AuditReviewStore {
    override fun find(code: String): AuditReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditReview) {
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

    override fun list(): MutableList<AuditReview> {
        val out = mutableListOf<AuditReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditReviewStore(): AuditReviewStore {
    return MemoryAuditReviewStore(mutableMapOf<String, AuditReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditReviewStore(
    var inner: AuditReviewStore,
    var cache: TtlCache<AuditReview>,
    var hits: Int,
    var misses: Int,
) : AuditReviewStore {
    override fun find(code: String): AuditReview? {
        val hit = (cache.get(code) as AuditReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditReview> = inner.list()
}

fun newCachedAuditReviewStore(inner: AuditReviewStore, ttl: Int, clock: Clock): AuditReviewStore {
    return CachedAuditReviewStore(
        inner = inner,
        cache = newTtlCache<AuditReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit reviews. Empty fields match all. */
data class AuditReviewQuery(
    var region: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditReview): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AuditReview>): MutableList<AuditReview> {
        val matched = filter(items) { candidate: AuditReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditReviews(store: AuditReviewStore, query: AuditReviewQuery): MutableList<AuditReview> {
    return query.apply(store.list())
}
