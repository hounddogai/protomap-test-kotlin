package com.acme.transport.linehaul.relay.quota

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive relay reviews by code. */
interface AdaptiveRelayReviewStore {
    fun find(code: String): AdaptiveRelayReview?
    fun save(item: AdaptiveRelayReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveRelayReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveRelayReviewStore(
    var items: MutableMap<String, AdaptiveRelayReview>,
    var order: MutableList<String>,
) : AdaptiveRelayReviewStore {
    override fun find(code: String): AdaptiveRelayReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveRelayReview) {
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

    override fun list(): MutableList<AdaptiveRelayReview> {
        val out = mutableListOf<AdaptiveRelayReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveRelayReviewStore(): AdaptiveRelayReviewStore {
    return MemoryAdaptiveRelayReviewStore(mutableMapOf<String, AdaptiveRelayReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveRelayReviewStore(
    var inner: AdaptiveRelayReviewStore,
    var cache: TtlCache<AdaptiveRelayReview>,
    var hits: Int,
    var misses: Int,
) : AdaptiveRelayReviewStore {
    override fun find(code: String): AdaptiveRelayReview? {
        val hit = (cache.get(code) as AdaptiveRelayReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveRelayReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveRelayReview> = inner.list()
}

fun newCachedAdaptiveRelayReviewStore(inner: AdaptiveRelayReviewStore, ttl: Int, clock: Clock): AdaptiveRelayReviewStore {
    return CachedAdaptiveRelayReviewStore(
        inner = inner,
        cache = newTtlCache<AdaptiveRelayReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive relay reviews. Empty fields match all. */
data class AdaptiveRelayReviewQuery(
    var depot: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveRelayReview): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveRelayReview>): MutableList<AdaptiveRelayReview> {
        val matched = filter(items) { candidate: AdaptiveRelayReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveRelayReviews(store: AdaptiveRelayReviewStore, query: AdaptiveRelayReviewQuery): MutableList<AdaptiveRelayReview> {
    return query.apply(store.list())
}
