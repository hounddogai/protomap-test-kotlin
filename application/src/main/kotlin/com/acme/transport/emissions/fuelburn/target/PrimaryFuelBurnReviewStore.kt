package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary fuel burn reviews by code. */
interface PrimaryFuelBurnReviewStore {
    fun find(code: String): PrimaryFuelBurnReview?
    fun save(item: PrimaryFuelBurnReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryFuelBurnReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryFuelBurnReviewStore(
    var items: MutableMap<String, PrimaryFuelBurnReview>,
    var order: MutableList<String>,
) : PrimaryFuelBurnReviewStore {
    override fun find(code: String): PrimaryFuelBurnReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryFuelBurnReview) {
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

    override fun list(): MutableList<PrimaryFuelBurnReview> {
        val out = mutableListOf<PrimaryFuelBurnReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryFuelBurnReviewStore(): PrimaryFuelBurnReviewStore {
    return MemoryPrimaryFuelBurnReviewStore(
        mutableMapOf<String, PrimaryFuelBurnReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryFuelBurnReviewStore(
    var inner: PrimaryFuelBurnReviewStore,
    var cache: TtlCache<PrimaryFuelBurnReview>,
    var hits: Int,
    var misses: Int,
) : PrimaryFuelBurnReviewStore {
    override fun find(code: String): PrimaryFuelBurnReview? {
        val hit = (cache.get(code) as PrimaryFuelBurnReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryFuelBurnReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryFuelBurnReview> = inner.list()
}

fun newCachedPrimaryFuelBurnReviewStore(inner: PrimaryFuelBurnReviewStore, ttl: Int, clock: Clock): PrimaryFuelBurnReviewStore {
    return CachedPrimaryFuelBurnReviewStore(
        inner = inner,
        cache = newTtlCache<PrimaryFuelBurnReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary fuel burn reviews. Empty fields match all. */
data class PrimaryFuelBurnReviewQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryFuelBurnReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryFuelBurnReview>): MutableList<PrimaryFuelBurnReview> {
        val matched = filter(items) { candidate: PrimaryFuelBurnReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryFuelBurnReviews(store: PrimaryFuelBurnReviewStore, query: PrimaryFuelBurnReviewQuery): MutableList<PrimaryFuelBurnReview> {
    return query.apply(store.list())
}
