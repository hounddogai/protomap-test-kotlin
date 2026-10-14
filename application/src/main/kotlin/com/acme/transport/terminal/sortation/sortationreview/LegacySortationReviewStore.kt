package com.acme.transport.terminal.sortation.sortationreview

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy sortation reviews by code. */
interface LegacySortationReviewStore {
    fun find(code: String): LegacySortationReview?
    fun save(item: LegacySortationReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacySortationReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacySortationReviewStore(
    var items: MutableMap<String, LegacySortationReview>,
    var order: MutableList<String>,
) : LegacySortationReviewStore {
    override fun find(code: String): LegacySortationReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacySortationReview) {
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

    override fun list(): MutableList<LegacySortationReview> {
        val out = mutableListOf<LegacySortationReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacySortationReviewStore(): LegacySortationReviewStore {
    return MemoryLegacySortationReviewStore(
        mutableMapOf<String, LegacySortationReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacySortationReviewStore(
    var inner: LegacySortationReviewStore,
    var cache: TtlCache<LegacySortationReview>,
    var hits: Int,
    var misses: Int,
) : LegacySortationReviewStore {
    override fun find(code: String): LegacySortationReview? {
        val hit = (cache.get(code) as LegacySortationReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacySortationReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacySortationReview> = inner.list()
}

fun newCachedLegacySortationReviewStore(inner: LegacySortationReviewStore, ttl: Int, clock: Clock): LegacySortationReviewStore {
    return CachedLegacySortationReviewStore(
        inner = inner,
        cache = newTtlCache<LegacySortationReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy sortation reviews. Empty fields match all. */
data class LegacySortationReviewQuery(
    var category: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacySortationReview): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacySortationReview>): MutableList<LegacySortationReview> {
        val matched = filter(items) { candidate: LegacySortationReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacySortationReviews(store: LegacySortationReviewStore, query: LegacySortationReviewQuery): MutableList<LegacySortationReview> {
    return query.apply(store.list())
}
