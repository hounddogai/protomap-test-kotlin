package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback lane plan reviews by code. */
interface FallbackLanePlanReviewStore {
    fun find(code: String): FallbackLanePlanReview?
    fun save(item: FallbackLanePlanReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackLanePlanReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackLanePlanReviewStore(
    var items: MutableMap<String, FallbackLanePlanReview>,
    var order: MutableList<String>,
) : FallbackLanePlanReviewStore {
    override fun find(code: String): FallbackLanePlanReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackLanePlanReview) {
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

    override fun list(): MutableList<FallbackLanePlanReview> {
        val out = mutableListOf<FallbackLanePlanReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackLanePlanReviewStore(): FallbackLanePlanReviewStore {
    return MemoryFallbackLanePlanReviewStore(
        mutableMapOf<String, FallbackLanePlanReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackLanePlanReviewStore(
    var inner: FallbackLanePlanReviewStore,
    var cache: TtlCache<FallbackLanePlanReview>,
    var hits: Int,
    var misses: Int,
) : FallbackLanePlanReviewStore {
    override fun find(code: String): FallbackLanePlanReview? {
        val hit = (cache.get(code) as FallbackLanePlanReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackLanePlanReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackLanePlanReview> = inner.list()
}

fun newCachedFallbackLanePlanReviewStore(inner: FallbackLanePlanReviewStore, ttl: Int, clock: Clock): FallbackLanePlanReviewStore {
    return CachedFallbackLanePlanReviewStore(
        inner = inner,
        cache = newTtlCache<FallbackLanePlanReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback lane plan reviews. Empty fields match all. */
data class FallbackLanePlanReviewQuery(
    var label: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackLanePlanReview): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackLanePlanReview>): MutableList<FallbackLanePlanReview> {
        val matched = filter(items) { candidate: FallbackLanePlanReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackLanePlanReviews(store: FallbackLanePlanReviewStore, query: FallbackLanePlanReviewQuery): MutableList<FallbackLanePlanReview> {
    return query.apply(store.list())
}
