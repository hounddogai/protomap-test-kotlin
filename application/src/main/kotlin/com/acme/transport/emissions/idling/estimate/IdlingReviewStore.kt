package com.acme.transport.emissions.idling.estimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling reviews by code. */
interface IdlingReviewStore {
    fun find(code: String): IdlingReview?
    fun save(item: IdlingReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingReviewStore(
    var items: MutableMap<String, IdlingReview>,
    var order: MutableList<String>,
) : IdlingReviewStore {
    override fun find(code: String): IdlingReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingReview) {
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

    override fun list(): MutableList<IdlingReview> {
        val out = mutableListOf<IdlingReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingReviewStore(): IdlingReviewStore {
    return MemoryIdlingReviewStore(mutableMapOf<String, IdlingReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingReviewStore(
    var inner: IdlingReviewStore,
    var cache: TtlCache<IdlingReview>,
    var hits: Int,
    var misses: Int,
) : IdlingReviewStore {
    override fun find(code: String): IdlingReview? {
        val hit = (cache.get(code) as IdlingReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingReview> = inner.list()
}

fun newCachedIdlingReviewStore(inner: IdlingReviewStore, ttl: Int, clock: Clock): IdlingReviewStore {
    return CachedIdlingReviewStore(
        inner = inner,
        cache = newTtlCache<IdlingReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling reviews. Empty fields match all. */
data class IdlingReviewQuery(
    var country: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingReview): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IdlingReview>): MutableList<IdlingReview> {
        val matched = filter(items) { candidate: IdlingReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingReviews(store: IdlingReviewStore, query: IdlingReviewQuery): MutableList<IdlingReview> {
    return query.apply(store.list())
}
