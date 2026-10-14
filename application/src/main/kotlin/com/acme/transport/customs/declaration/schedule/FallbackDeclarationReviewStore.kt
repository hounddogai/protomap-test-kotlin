package com.acme.transport.customs.declaration.schedule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback declaration reviews by code. */
interface FallbackDeclarationReviewStore {
    fun find(code: String): FallbackDeclarationReview?
    fun save(item: FallbackDeclarationReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackDeclarationReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackDeclarationReviewStore(
    var items: MutableMap<String, FallbackDeclarationReview>,
    var order: MutableList<String>,
) : FallbackDeclarationReviewStore {
    override fun find(code: String): FallbackDeclarationReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackDeclarationReview) {
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

    override fun list(): MutableList<FallbackDeclarationReview> {
        val out = mutableListOf<FallbackDeclarationReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackDeclarationReviewStore(): FallbackDeclarationReviewStore {
    return MemoryFallbackDeclarationReviewStore(
        mutableMapOf<String, FallbackDeclarationReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackDeclarationReviewStore(
    var inner: FallbackDeclarationReviewStore,
    var cache: TtlCache<FallbackDeclarationReview>,
    var hits: Int,
    var misses: Int,
) : FallbackDeclarationReviewStore {
    override fun find(code: String): FallbackDeclarationReview? {
        val hit = (cache.get(code) as FallbackDeclarationReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackDeclarationReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackDeclarationReview> = inner.list()
}

fun newCachedFallbackDeclarationReviewStore(inner: FallbackDeclarationReviewStore, ttl: Int, clock: Clock): FallbackDeclarationReviewStore {
    return CachedFallbackDeclarationReviewStore(
        inner = inner,
        cache = newTtlCache<FallbackDeclarationReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback declaration reviews. Empty fields match all. */
data class FallbackDeclarationReviewQuery(
    var lane: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackDeclarationReview): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<FallbackDeclarationReview>): MutableList<FallbackDeclarationReview> {
        val matched = filter(items) { candidate: FallbackDeclarationReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackDeclarationReviews(store: FallbackDeclarationReviewStore, query: FallbackDeclarationReviewQuery): MutableList<FallbackDeclarationReview> {
    return query.apply(store.list())
}
