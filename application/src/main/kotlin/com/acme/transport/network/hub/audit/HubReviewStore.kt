package com.acme.transport.network.hub.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps hub reviews by code. */
interface HubReviewStore {
    fun find(code: String): HubReview?
    fun save(item: HubReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<HubReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryHubReviewStore(
    var items: MutableMap<String, HubReview>,
    var order: MutableList<String>,
) : HubReviewStore {
    override fun find(code: String): HubReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: HubReview) {
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

    override fun list(): MutableList<HubReview> {
        val out = mutableListOf<HubReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryHubReviewStore(): HubReviewStore {
    return MemoryHubReviewStore(mutableMapOf<String, HubReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedHubReviewStore(
    var inner: HubReviewStore,
    var cache: TtlCache<HubReview>,
    var hits: Int,
    var misses: Int,
) : HubReviewStore {
    override fun find(code: String): HubReview? {
        val hit = (cache.get(code) as HubReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: HubReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<HubReview> = inner.list()
}

fun newCachedHubReviewStore(inner: HubReviewStore, ttl: Int, clock: Clock): HubReviewStore {
    return CachedHubReviewStore(
        inner = inner,
        cache = newTtlCache<HubReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects hub reviews. Empty fields match all. */
data class HubReviewQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: HubReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<HubReview>): MutableList<HubReview> {
        val matched = filter(items) { candidate: HubReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findHubReviews(store: HubReviewStore, query: HubReviewQuery): MutableList<HubReview> {
    return query.apply(store.list())
}
