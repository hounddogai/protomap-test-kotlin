package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default sortation reviews by code. */
interface DefaultSortationReviewStore {
    fun find(code: String): DefaultSortationReview?
    fun save(item: DefaultSortationReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultSortationReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultSortationReviewStore(
    var items: MutableMap<String, DefaultSortationReview>,
    var order: MutableList<String>,
) : DefaultSortationReviewStore {
    override fun find(code: String): DefaultSortationReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultSortationReview) {
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

    override fun list(): MutableList<DefaultSortationReview> {
        val out = mutableListOf<DefaultSortationReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultSortationReviewStore(): DefaultSortationReviewStore {
    return MemoryDefaultSortationReviewStore(
        mutableMapOf<String, DefaultSortationReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultSortationReviewStore(
    var inner: DefaultSortationReviewStore,
    var cache: TtlCache<DefaultSortationReview>,
    var hits: Int,
    var misses: Int,
) : DefaultSortationReviewStore {
    override fun find(code: String): DefaultSortationReview? {
        val hit = (cache.get(code) as DefaultSortationReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultSortationReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultSortationReview> = inner.list()
}

fun newCachedDefaultSortationReviewStore(inner: DefaultSortationReviewStore, ttl: Int, clock: Clock): DefaultSortationReviewStore {
    return CachedDefaultSortationReviewStore(
        inner = inner,
        cache = newTtlCache<DefaultSortationReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default sortation reviews. Empty fields match all. */
data class DefaultSortationReviewQuery(
    var depot: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultSortationReview): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultSortationReview>): MutableList<DefaultSortationReview> {
        val matched = filter(items) { candidate: DefaultSortationReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultSortationReviews(store: DefaultSortationReviewStore, query: DefaultSortationReviewQuery): MutableList<DefaultSortationReview> {
    return query.apply(store.list())
}
