package com.acme.transport.network.zone.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps global zone reviews by code. */
interface GlobalZoneReviewStore {
    fun find(code: String): GlobalZoneReview?
    fun save(item: GlobalZoneReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalZoneReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalZoneReviewStore(
    var items: MutableMap<String, GlobalZoneReview>,
    var order: MutableList<String>,
) : GlobalZoneReviewStore {
    override fun find(code: String): GlobalZoneReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalZoneReview) {
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

    override fun list(): MutableList<GlobalZoneReview> {
        val out = mutableListOf<GlobalZoneReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalZoneReviewStore(): GlobalZoneReviewStore {
    return MemoryGlobalZoneReviewStore(mutableMapOf<String, GlobalZoneReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalZoneReviewStore(
    var inner: GlobalZoneReviewStore,
    var cache: TtlCache<GlobalZoneReview>,
    var hits: Int,
    var misses: Int,
) : GlobalZoneReviewStore {
    override fun find(code: String): GlobalZoneReview? {
        val hit = (cache.get(code) as GlobalZoneReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalZoneReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalZoneReview> = inner.list()
}

fun newCachedGlobalZoneReviewStore(inner: GlobalZoneReviewStore, ttl: Int, clock: Clock): GlobalZoneReviewStore {
    return CachedGlobalZoneReviewStore(
        inner = inner,
        cache = newTtlCache<GlobalZoneReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global zone reviews. Empty fields match all. */
data class GlobalZoneReviewQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalZoneReview): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalZoneReview>): MutableList<GlobalZoneReview> {
        val matched = filter(items) { candidate: GlobalZoneReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalZoneReviews(store: GlobalZoneReviewStore, query: GlobalZoneReviewQuery): MutableList<GlobalZoneReview> {
    return query.apply(store.list())
}
