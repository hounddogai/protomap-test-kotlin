package com.acme.transport.freightpay.fuelsurcharge.review

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge reviews by code. */
interface FuelSurchargeReviewStore {
    fun find(code: String): FuelSurchargeReview?
    fun save(item: FuelSurchargeReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeReviewStore(
    var items: MutableMap<String, FuelSurchargeReview>,
    var order: MutableList<String>,
) : FuelSurchargeReviewStore {
    override fun find(code: String): FuelSurchargeReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeReview) {
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

    override fun list(): MutableList<FuelSurchargeReview> {
        val out = mutableListOf<FuelSurchargeReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeReviewStore(): FuelSurchargeReviewStore {
    return MemoryFuelSurchargeReviewStore(mutableMapOf<String, FuelSurchargeReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeReviewStore(
    var inner: FuelSurchargeReviewStore,
    var cache: TtlCache<FuelSurchargeReview>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeReviewStore {
    override fun find(code: String): FuelSurchargeReview? {
        val hit = (cache.get(code) as FuelSurchargeReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeReview> = inner.list()
}

fun newCachedFuelSurchargeReviewStore(inner: FuelSurchargeReviewStore, ttl: Int, clock: Clock): FuelSurchargeReviewStore {
    return CachedFuelSurchargeReviewStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge reviews. Empty fields match all. */
data class FuelSurchargeReviewQuery(
    var country: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeReview): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeReview>): MutableList<FuelSurchargeReview> {
        val matched = filter(items) { candidate: FuelSurchargeReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeReviews(store: FuelSurchargeReviewStore, query: FuelSurchargeReviewQuery): MutableList<FuelSurchargeReview> {
    return query.apply(store.list())
}
