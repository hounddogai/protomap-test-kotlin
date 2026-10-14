package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling sortation reviews by code. */
interface RollingSortationReviewStore {
    fun find(code: String): RollingSortationReview?
    fun save(item: RollingSortationReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingSortationReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingSortationReviewStore(
    var items: MutableMap<String, RollingSortationReview>,
    var order: MutableList<String>,
) : RollingSortationReviewStore {
    override fun find(code: String): RollingSortationReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingSortationReview) {
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

    override fun list(): MutableList<RollingSortationReview> {
        val out = mutableListOf<RollingSortationReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingSortationReviewStore(): RollingSortationReviewStore {
    return MemoryRollingSortationReviewStore(
        mutableMapOf<String, RollingSortationReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingSortationReviewStore(
    var inner: RollingSortationReviewStore,
    var cache: TtlCache<RollingSortationReview>,
    var hits: Int,
    var misses: Int,
) : RollingSortationReviewStore {
    override fun find(code: String): RollingSortationReview? {
        val hit = (cache.get(code) as RollingSortationReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingSortationReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingSortationReview> = inner.list()
}

fun newCachedRollingSortationReviewStore(inner: RollingSortationReviewStore, ttl: Int, clock: Clock): RollingSortationReviewStore {
    return CachedRollingSortationReviewStore(
        inner = inner,
        cache = newTtlCache<RollingSortationReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling sortation reviews. Empty fields match all. */
data class RollingSortationReviewQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingSortationReview): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RollingSortationReview>): MutableList<RollingSortationReview> {
        val matched = filter(items) { candidate: RollingSortationReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingSortationReviews(store: RollingSortationReviewStore, query: RollingSortationReviewQuery): MutableList<RollingSortationReview> {
    return query.apply(store.list())
}
