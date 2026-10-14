package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone reviews by code. */
interface ZoneReviewStore {
    fun find(code: String): ZoneReview?
    fun save(item: ZoneReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneReviewStore(
    var items: MutableMap<String, ZoneReview>,
    var order: MutableList<String>,
) : ZoneReviewStore {
    override fun find(code: String): ZoneReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneReview) {
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

    override fun list(): MutableList<ZoneReview> {
        val out = mutableListOf<ZoneReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneReviewStore(): ZoneReviewStore {
    return MemoryZoneReviewStore(mutableMapOf<String, ZoneReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneReviewStore(
    var inner: ZoneReviewStore,
    var cache: TtlCache<ZoneReview>,
    var hits: Int,
    var misses: Int,
) : ZoneReviewStore {
    override fun find(code: String): ZoneReview? {
        val hit = (cache.get(code) as ZoneReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneReview> = inner.list()
}

fun newCachedZoneReviewStore(inner: ZoneReviewStore, ttl: Int, clock: Clock): ZoneReviewStore {
    return CachedZoneReviewStore(
        inner = inner,
        cache = newTtlCache<ZoneReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone reviews. Empty fields match all. */
data class ZoneReviewQuery(
    var country: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneReview): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneReview>): MutableList<ZoneReview> {
        val matched = filter(items) { candidate: ZoneReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneReviews(store: ZoneReviewStore, query: ZoneReviewQuery): MutableList<ZoneReview> {
    return query.apply(store.list())
}
