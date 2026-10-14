package com.acme.transport.crewing.qualification.review

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps qualification reviews by code. */
interface QualificationReviewStore {
    fun find(code: String): QualificationReview?
    fun save(item: QualificationReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<QualificationReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryQualificationReviewStore(
    var items: MutableMap<String, QualificationReview>,
    var order: MutableList<String>,
) : QualificationReviewStore {
    override fun find(code: String): QualificationReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: QualificationReview) {
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

    override fun list(): MutableList<QualificationReview> {
        val out = mutableListOf<QualificationReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryQualificationReviewStore(): QualificationReviewStore {
    return MemoryQualificationReviewStore(mutableMapOf<String, QualificationReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedQualificationReviewStore(
    var inner: QualificationReviewStore,
    var cache: TtlCache<QualificationReview>,
    var hits: Int,
    var misses: Int,
) : QualificationReviewStore {
    override fun find(code: String): QualificationReview? {
        val hit = (cache.get(code) as QualificationReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: QualificationReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<QualificationReview> = inner.list()
}

fun newCachedQualificationReviewStore(inner: QualificationReviewStore, ttl: Int, clock: Clock): QualificationReviewStore {
    return CachedQualificationReviewStore(
        inner = inner,
        cache = newTtlCache<QualificationReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects qualification reviews. Empty fields match all. */
data class QualificationReviewQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: QualificationReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<QualificationReview>): MutableList<QualificationReview> {
        val matched = filter(items) { candidate: QualificationReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findQualificationReviews(store: QualificationReviewStore, query: QualificationReviewQuery): MutableList<QualificationReview> {
    return query.apply(store.list())
}
