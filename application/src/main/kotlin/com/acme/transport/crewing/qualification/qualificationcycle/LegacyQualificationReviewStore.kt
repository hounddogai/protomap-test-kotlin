package com.acme.transport.crewing.qualification.qualificationcycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy qualification reviews by code. */
interface LegacyQualificationReviewStore {
    fun find(code: String): LegacyQualificationReview?
    fun save(item: LegacyQualificationReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyQualificationReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyQualificationReviewStore(
    var items: MutableMap<String, LegacyQualificationReview>,
    var order: MutableList<String>,
) : LegacyQualificationReviewStore {
    override fun find(code: String): LegacyQualificationReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyQualificationReview) {
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

    override fun list(): MutableList<LegacyQualificationReview> {
        val out = mutableListOf<LegacyQualificationReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyQualificationReviewStore(): LegacyQualificationReviewStore {
    return MemoryLegacyQualificationReviewStore(
        mutableMapOf<String, LegacyQualificationReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyQualificationReviewStore(
    var inner: LegacyQualificationReviewStore,
    var cache: TtlCache<LegacyQualificationReview>,
    var hits: Int,
    var misses: Int,
) : LegacyQualificationReviewStore {
    override fun find(code: String): LegacyQualificationReview? {
        val hit = (cache.get(code) as LegacyQualificationReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyQualificationReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyQualificationReview> = inner.list()
}

fun newCachedLegacyQualificationReviewStore(inner: LegacyQualificationReviewStore, ttl: Int, clock: Clock): LegacyQualificationReviewStore {
    return CachedLegacyQualificationReviewStore(
        inner = inner,
        cache = newTtlCache<LegacyQualificationReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy qualification reviews. Empty fields match all. */
data class LegacyQualificationReviewQuery(
    var channel: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyQualificationReview): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyQualificationReview>): MutableList<LegacyQualificationReview> {
        val matched = filter(items) { candidate: LegacyQualificationReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyQualificationReviews(store: LegacyQualificationReviewStore, query: LegacyQualificationReviewQuery): MutableList<LegacyQualificationReview> {
    return query.apply(store.list())
}
