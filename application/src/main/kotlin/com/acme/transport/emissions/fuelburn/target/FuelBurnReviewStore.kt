package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn reviews by code. */
interface FuelBurnReviewStore {
    fun find(code: String): FuelBurnReview?
    fun save(item: FuelBurnReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnReviewStore(
    var items: MutableMap<String, FuelBurnReview>,
    var order: MutableList<String>,
) : FuelBurnReviewStore {
    override fun find(code: String): FuelBurnReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnReview) {
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

    override fun list(): MutableList<FuelBurnReview> {
        val out = mutableListOf<FuelBurnReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnReviewStore(): FuelBurnReviewStore {
    return MemoryFuelBurnReviewStore(mutableMapOf<String, FuelBurnReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnReviewStore(
    var inner: FuelBurnReviewStore,
    var cache: TtlCache<FuelBurnReview>,
    var hits: Int,
    var misses: Int,
) : FuelBurnReviewStore {
    override fun find(code: String): FuelBurnReview? {
        val hit = (cache.get(code) as FuelBurnReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnReview> = inner.list()
}

fun newCachedFuelBurnReviewStore(inner: FuelBurnReviewStore, ttl: Int, clock: Clock): FuelBurnReviewStore {
    return CachedFuelBurnReviewStore(
        inner = inner,
        cache = newTtlCache<FuelBurnReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn reviews. Empty fields match all. */
data class FuelBurnReviewQuery(
    var unit: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnReview): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<FuelBurnReview>): MutableList<FuelBurnReview> {
        val matched = filter(items) { candidate: FuelBurnReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnReviews(store: FuelBurnReviewStore, query: FuelBurnReviewQuery): MutableList<FuelBurnReview> {
    return query.apply(store.list())
}
