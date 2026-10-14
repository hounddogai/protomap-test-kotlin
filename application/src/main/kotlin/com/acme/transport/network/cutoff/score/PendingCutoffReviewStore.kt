package com.acme.transport.network.cutoff.score

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps pending cutoff reviews by code. */
interface PendingCutoffReviewStore {
    fun find(code: String): PendingCutoffReview?
    fun save(item: PendingCutoffReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingCutoffReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingCutoffReviewStore(
    var items: MutableMap<String, PendingCutoffReview>,
    var order: MutableList<String>,
) : PendingCutoffReviewStore {
    override fun find(code: String): PendingCutoffReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingCutoffReview) {
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

    override fun list(): MutableList<PendingCutoffReview> {
        val out = mutableListOf<PendingCutoffReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingCutoffReviewStore(): PendingCutoffReviewStore {
    return MemoryPendingCutoffReviewStore(mutableMapOf<String, PendingCutoffReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingCutoffReviewStore(
    var inner: PendingCutoffReviewStore,
    var cache: TtlCache<PendingCutoffReview>,
    var hits: Int,
    var misses: Int,
) : PendingCutoffReviewStore {
    override fun find(code: String): PendingCutoffReview? {
        val hit = (cache.get(code) as PendingCutoffReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingCutoffReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingCutoffReview> = inner.list()
}

fun newCachedPendingCutoffReviewStore(inner: PendingCutoffReviewStore, ttl: Int, clock: Clock): PendingCutoffReviewStore {
    return CachedPendingCutoffReviewStore(
        inner = inner,
        cache = newTtlCache<PendingCutoffReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending cutoff reviews. Empty fields match all. */
data class PendingCutoffReviewQuery(
    var channel: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingCutoffReview): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingCutoffReview>): MutableList<PendingCutoffReview> {
        val matched = filter(items) { candidate: PendingCutoffReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingCutoffReviews(store: PendingCutoffReviewStore, query: PendingCutoffReviewQuery): MutableList<PendingCutoffReview> {
    return query.apply(store.list())
}
