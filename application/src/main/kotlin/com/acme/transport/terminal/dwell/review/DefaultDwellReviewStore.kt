package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps default dwell reviews by code. */
interface DefaultDwellReviewStore {
    fun find(code: String): DefaultDwellReview?
    fun save(item: DefaultDwellReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDwellReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDwellReviewStore(
    var items: MutableMap<String, DefaultDwellReview>,
    var order: MutableList<String>,
) : DefaultDwellReviewStore {
    override fun find(code: String): DefaultDwellReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDwellReview) {
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

    override fun list(): MutableList<DefaultDwellReview> {
        val out = mutableListOf<DefaultDwellReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDwellReviewStore(): DefaultDwellReviewStore {
    return MemoryDefaultDwellReviewStore(mutableMapOf<String, DefaultDwellReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDwellReviewStore(
    var inner: DefaultDwellReviewStore,
    var cache: TtlCache<DefaultDwellReview>,
    var hits: Int,
    var misses: Int,
) : DefaultDwellReviewStore {
    override fun find(code: String): DefaultDwellReview? {
        val hit = (cache.get(code) as DefaultDwellReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDwellReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDwellReview> = inner.list()
}

fun newCachedDefaultDwellReviewStore(inner: DefaultDwellReviewStore, ttl: Int, clock: Clock): DefaultDwellReviewStore {
    return CachedDefaultDwellReviewStore(
        inner = inner,
        cache = newTtlCache<DefaultDwellReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default dwell reviews. Empty fields match all. */
data class DefaultDwellReviewQuery(
    var lane: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDwellReview): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDwellReview>): MutableList<DefaultDwellReview> {
        val matched = filter(items) { candidate: DefaultDwellReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDwellReviews(store: DefaultDwellReviewStore, query: DefaultDwellReviewQuery): MutableList<DefaultDwellReview> {
    return query.apply(store.list())
}
