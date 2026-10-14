package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report reviews by code. */
interface CarbonReportReviewStore {
    fun find(code: String): CarbonReportReview?
    fun save(item: CarbonReportReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportReviewStore(
    var items: MutableMap<String, CarbonReportReview>,
    var order: MutableList<String>,
) : CarbonReportReviewStore {
    override fun find(code: String): CarbonReportReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportReview) {
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

    override fun list(): MutableList<CarbonReportReview> {
        val out = mutableListOf<CarbonReportReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportReviewStore(): CarbonReportReviewStore {
    return MemoryCarbonReportReviewStore(mutableMapOf<String, CarbonReportReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportReviewStore(
    var inner: CarbonReportReviewStore,
    var cache: TtlCache<CarbonReportReview>,
    var hits: Int,
    var misses: Int,
) : CarbonReportReviewStore {
    override fun find(code: String): CarbonReportReview? {
        val hit = (cache.get(code) as CarbonReportReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportReview> = inner.list()
}

fun newCachedCarbonReportReviewStore(inner: CarbonReportReviewStore, ttl: Int, clock: Clock): CarbonReportReviewStore {
    return CachedCarbonReportReviewStore(
        inner = inner,
        cache = newTtlCache<CarbonReportReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report reviews. Empty fields match all. */
data class CarbonReportReviewQuery(
    var channel: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportReview): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<CarbonReportReview>): MutableList<CarbonReportReview> {
        val matched = filter(items) { candidate: CarbonReportReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportReviews(store: CarbonReportReviewStore, query: CarbonReportReviewQuery): MutableList<CarbonReportReview> {
    return query.apply(store.list())
}
