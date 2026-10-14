package com.acme.transport.crewing.roster.agreement

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default roster reviews by code. */
interface DefaultRosterReviewStore {
    fun find(code: String): DefaultRosterReview?
    fun save(item: DefaultRosterReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRosterReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRosterReviewStore(
    var items: MutableMap<String, DefaultRosterReview>,
    var order: MutableList<String>,
) : DefaultRosterReviewStore {
    override fun find(code: String): DefaultRosterReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRosterReview) {
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

    override fun list(): MutableList<DefaultRosterReview> {
        val out = mutableListOf<DefaultRosterReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRosterReviewStore(): DefaultRosterReviewStore {
    return MemoryDefaultRosterReviewStore(mutableMapOf<String, DefaultRosterReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRosterReviewStore(
    var inner: DefaultRosterReviewStore,
    var cache: TtlCache<DefaultRosterReview>,
    var hits: Int,
    var misses: Int,
) : DefaultRosterReviewStore {
    override fun find(code: String): DefaultRosterReview? {
        val hit = (cache.get(code) as DefaultRosterReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRosterReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRosterReview> = inner.list()
}

fun newCachedDefaultRosterReviewStore(inner: DefaultRosterReviewStore, ttl: Int, clock: Clock): DefaultRosterReviewStore {
    return CachedDefaultRosterReviewStore(
        inner = inner,
        cache = newTtlCache<DefaultRosterReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default roster reviews. Empty fields match all. */
data class DefaultRosterReviewQuery(
    var title: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRosterReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultRosterReview>): MutableList<DefaultRosterReview> {
        val matched = filter(items) { candidate: DefaultRosterReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRosterReviews(store: DefaultRosterReviewStore, query: DefaultRosterReviewQuery): MutableList<DefaultRosterReview> {
    return query.apply(store.list())
}
