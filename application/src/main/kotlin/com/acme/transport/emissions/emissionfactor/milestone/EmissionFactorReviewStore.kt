package com.acme.transport.emissions.emissionfactor.milestone

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor reviews by code. */
interface EmissionFactorReviewStore {
    fun find(code: String): EmissionFactorReview?
    fun save(item: EmissionFactorReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorReviewStore(
    var items: MutableMap<String, EmissionFactorReview>,
    var order: MutableList<String>,
) : EmissionFactorReviewStore {
    override fun find(code: String): EmissionFactorReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorReview) {
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

    override fun list(): MutableList<EmissionFactorReview> {
        val out = mutableListOf<EmissionFactorReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorReviewStore(): EmissionFactorReviewStore {
    return MemoryEmissionFactorReviewStore(
        mutableMapOf<String, EmissionFactorReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorReviewStore(
    var inner: EmissionFactorReviewStore,
    var cache: TtlCache<EmissionFactorReview>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorReviewStore {
    override fun find(code: String): EmissionFactorReview? {
        val hit = (cache.get(code) as EmissionFactorReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorReview> = inner.list()
}

fun newCachedEmissionFactorReviewStore(inner: EmissionFactorReviewStore, ttl: Int, clock: Clock): EmissionFactorReviewStore {
    return CachedEmissionFactorReviewStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor reviews. Empty fields match all. */
data class EmissionFactorReviewQuery(
    var country: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorReview): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorReview>): MutableList<EmissionFactorReview> {
        val matched = filter(items) { candidate: EmissionFactorReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorReviews(store: EmissionFactorReviewStore, query: EmissionFactorReviewQuery): MutableList<EmissionFactorReview> {
    return query.apply(store.list())
}
