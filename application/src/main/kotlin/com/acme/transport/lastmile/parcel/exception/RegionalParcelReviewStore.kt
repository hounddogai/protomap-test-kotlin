package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps regional parcel reviews by code. */
interface RegionalParcelReviewStore {
    fun find(code: String): RegionalParcelReview?
    fun save(item: RegionalParcelReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalParcelReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalParcelReviewStore(
    var items: MutableMap<String, RegionalParcelReview>,
    var order: MutableList<String>,
) : RegionalParcelReviewStore {
    override fun find(code: String): RegionalParcelReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalParcelReview) {
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

    override fun list(): MutableList<RegionalParcelReview> {
        val out = mutableListOf<RegionalParcelReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalParcelReviewStore(): RegionalParcelReviewStore {
    return MemoryRegionalParcelReviewStore(
        mutableMapOf<String, RegionalParcelReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalParcelReviewStore(
    var inner: RegionalParcelReviewStore,
    var cache: TtlCache<RegionalParcelReview>,
    var hits: Int,
    var misses: Int,
) : RegionalParcelReviewStore {
    override fun find(code: String): RegionalParcelReview? {
        val hit = (cache.get(code) as RegionalParcelReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalParcelReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalParcelReview> = inner.list()
}

fun newCachedRegionalParcelReviewStore(inner: RegionalParcelReviewStore, ttl: Int, clock: Clock): RegionalParcelReviewStore {
    return CachedRegionalParcelReviewStore(
        inner = inner,
        cache = newTtlCache<RegionalParcelReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional parcel reviews. Empty fields match all. */
data class RegionalParcelReviewQuery(
    var depot: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalParcelReview): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalParcelReview>): MutableList<RegionalParcelReview> {
        val matched = filter(items) { candidate: RegionalParcelReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalParcelReviews(store: RegionalParcelReviewStore, query: RegionalParcelReviewQuery): MutableList<RegionalParcelReview> {
    return query.apply(store.list())
}
