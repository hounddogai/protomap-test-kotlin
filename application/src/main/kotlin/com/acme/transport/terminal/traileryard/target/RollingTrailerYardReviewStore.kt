package com.acme.transport.terminal.traileryard.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling trailer yard reviews by code. */
interface RollingTrailerYardReviewStore {
    fun find(code: String): RollingTrailerYardReview?
    fun save(item: RollingTrailerYardReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingTrailerYardReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingTrailerYardReviewStore(
    var items: MutableMap<String, RollingTrailerYardReview>,
    var order: MutableList<String>,
) : RollingTrailerYardReviewStore {
    override fun find(code: String): RollingTrailerYardReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingTrailerYardReview) {
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

    override fun list(): MutableList<RollingTrailerYardReview> {
        val out = mutableListOf<RollingTrailerYardReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingTrailerYardReviewStore(): RollingTrailerYardReviewStore {
    return MemoryRollingTrailerYardReviewStore(
        mutableMapOf<String, RollingTrailerYardReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingTrailerYardReviewStore(
    var inner: RollingTrailerYardReviewStore,
    var cache: TtlCache<RollingTrailerYardReview>,
    var hits: Int,
    var misses: Int,
) : RollingTrailerYardReviewStore {
    override fun find(code: String): RollingTrailerYardReview? {
        val hit = (cache.get(code) as RollingTrailerYardReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingTrailerYardReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingTrailerYardReview> = inner.list()
}

fun newCachedRollingTrailerYardReviewStore(inner: RollingTrailerYardReviewStore, ttl: Int, clock: Clock): RollingTrailerYardReviewStore {
    return CachedRollingTrailerYardReviewStore(
        inner = inner,
        cache = newTtlCache<RollingTrailerYardReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling trailer yard reviews. Empty fields match all. */
data class RollingTrailerYardReviewQuery(
    var unit: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingTrailerYardReview): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<RollingTrailerYardReview>): MutableList<RollingTrailerYardReview> {
        val matched = filter(items) { candidate: RollingTrailerYardReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingTrailerYardReviews(store: RollingTrailerYardReviewStore, query: RollingTrailerYardReviewQuery): MutableList<RollingTrailerYardReview> {
    return query.apply(store.list())
}
