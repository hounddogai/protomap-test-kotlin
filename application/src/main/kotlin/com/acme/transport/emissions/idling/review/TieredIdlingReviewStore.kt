package com.acme.transport.emissions.idling.review

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered idling reviews by code. */
interface TieredIdlingReviewStore {
    fun find(code: String): TieredIdlingReview?
    fun save(item: TieredIdlingReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredIdlingReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredIdlingReviewStore(
    var items: MutableMap<String, TieredIdlingReview>,
    var order: MutableList<String>,
) : TieredIdlingReviewStore {
    override fun find(code: String): TieredIdlingReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredIdlingReview) {
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

    override fun list(): MutableList<TieredIdlingReview> {
        val out = mutableListOf<TieredIdlingReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredIdlingReviewStore(): TieredIdlingReviewStore {
    return MemoryTieredIdlingReviewStore(mutableMapOf<String, TieredIdlingReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredIdlingReviewStore(
    var inner: TieredIdlingReviewStore,
    var cache: TtlCache<TieredIdlingReview>,
    var hits: Int,
    var misses: Int,
) : TieredIdlingReviewStore {
    override fun find(code: String): TieredIdlingReview? {
        val hit = (cache.get(code) as TieredIdlingReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredIdlingReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredIdlingReview> = inner.list()
}

fun newCachedTieredIdlingReviewStore(inner: TieredIdlingReviewStore, ttl: Int, clock: Clock): TieredIdlingReviewStore {
    return CachedTieredIdlingReviewStore(
        inner = inner,
        cache = newTtlCache<TieredIdlingReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered idling reviews. Empty fields match all. */
data class TieredIdlingReviewQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredIdlingReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<TieredIdlingReview>): MutableList<TieredIdlingReview> {
        val matched = filter(items) { candidate: TieredIdlingReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredIdlingReviews(store: TieredIdlingReviewStore, query: TieredIdlingReviewQuery): MutableList<TieredIdlingReview> {
    return query.apply(store.list())
}
