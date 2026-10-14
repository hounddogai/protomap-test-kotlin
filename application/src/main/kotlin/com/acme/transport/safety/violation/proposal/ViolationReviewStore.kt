package com.acme.transport.safety.violation.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation reviews by code. */
interface ViolationReviewStore {
    fun find(code: String): ViolationReview?
    fun save(item: ViolationReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationReviewStore(
    var items: MutableMap<String, ViolationReview>,
    var order: MutableList<String>,
) : ViolationReviewStore {
    override fun find(code: String): ViolationReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationReview) {
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

    override fun list(): MutableList<ViolationReview> {
        val out = mutableListOf<ViolationReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationReviewStore(): ViolationReviewStore {
    return MemoryViolationReviewStore(mutableMapOf<String, ViolationReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationReviewStore(
    var inner: ViolationReviewStore,
    var cache: TtlCache<ViolationReview>,
    var hits: Int,
    var misses: Int,
) : ViolationReviewStore {
    override fun find(code: String): ViolationReview? {
        val hit = (cache.get(code) as ViolationReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationReview> = inner.list()
}

fun newCachedViolationReviewStore(inner: ViolationReviewStore, ttl: Int, clock: Clock): ViolationReviewStore {
    return CachedViolationReviewStore(
        inner = inner,
        cache = newTtlCache<ViolationReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation reviews. Empty fields match all. */
data class ViolationReviewQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationReview>): MutableList<ViolationReview> {
        val matched = filter(items) { candidate: ViolationReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationReviews(store: ViolationReviewStore, query: ViolationReviewQuery): MutableList<ViolationReview> {
    return query.apply(store.list())
}
