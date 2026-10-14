package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted proof of delivery reviews by code. */
interface WeightedProofOfDeliveryReviewStore {
    fun find(code: String): WeightedProofOfDeliveryReview?
    fun save(item: WeightedProofOfDeliveryReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedProofOfDeliveryReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedProofOfDeliveryReviewStore(
    var items: MutableMap<String, WeightedProofOfDeliveryReview>,
    var order: MutableList<String>,
) : WeightedProofOfDeliveryReviewStore {
    override fun find(code: String): WeightedProofOfDeliveryReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedProofOfDeliveryReview) {
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

    override fun list(): MutableList<WeightedProofOfDeliveryReview> {
        val out = mutableListOf<WeightedProofOfDeliveryReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedProofOfDeliveryReviewStore(): WeightedProofOfDeliveryReviewStore {
    return MemoryWeightedProofOfDeliveryReviewStore(
        mutableMapOf<String, WeightedProofOfDeliveryReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedProofOfDeliveryReviewStore(
    var inner: WeightedProofOfDeliveryReviewStore,
    var cache: TtlCache<WeightedProofOfDeliveryReview>,
    var hits: Int,
    var misses: Int,
) : WeightedProofOfDeliveryReviewStore {
    override fun find(code: String): WeightedProofOfDeliveryReview? {
        val hit = (cache.get(code) as WeightedProofOfDeliveryReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedProofOfDeliveryReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedProofOfDeliveryReview> = inner.list()
}

fun newCachedWeightedProofOfDeliveryReviewStore(inner: WeightedProofOfDeliveryReviewStore, ttl: Int, clock: Clock): WeightedProofOfDeliveryReviewStore {
    return CachedWeightedProofOfDeliveryReviewStore(
        inner = inner,
        cache = newTtlCache<WeightedProofOfDeliveryReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted proof of delivery reviews. Empty fields match all. */
data class WeightedProofOfDeliveryReviewQuery(
    var unit: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedProofOfDeliveryReview): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedProofOfDeliveryReview>): MutableList<WeightedProofOfDeliveryReview> {
        val matched = filter(items) { candidate: WeightedProofOfDeliveryReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedProofOfDeliveryReviews(store: WeightedProofOfDeliveryReviewStore, query: WeightedProofOfDeliveryReviewQuery): MutableList<WeightedProofOfDeliveryReview> {
    return query.apply(store.list())
}
