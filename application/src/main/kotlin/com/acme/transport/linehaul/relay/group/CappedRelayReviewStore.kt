package com.acme.transport.linehaul.relay.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capped relay reviews by code. */
interface CappedRelayReviewStore {
    fun find(code: String): CappedRelayReview?
    fun save(item: CappedRelayReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedRelayReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedRelayReviewStore(
    var items: MutableMap<String, CappedRelayReview>,
    var order: MutableList<String>,
) : CappedRelayReviewStore {
    override fun find(code: String): CappedRelayReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedRelayReview) {
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

    override fun list(): MutableList<CappedRelayReview> {
        val out = mutableListOf<CappedRelayReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedRelayReviewStore(): CappedRelayReviewStore {
    return MemoryCappedRelayReviewStore(mutableMapOf<String, CappedRelayReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedRelayReviewStore(
    var inner: CappedRelayReviewStore,
    var cache: TtlCache<CappedRelayReview>,
    var hits: Int,
    var misses: Int,
) : CappedRelayReviewStore {
    override fun find(code: String): CappedRelayReview? {
        val hit = (cache.get(code) as CappedRelayReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedRelayReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedRelayReview> = inner.list()
}

fun newCachedCappedRelayReviewStore(inner: CappedRelayReviewStore, ttl: Int, clock: Clock): CappedRelayReviewStore {
    return CachedCappedRelayReviewStore(
        inner = inner,
        cache = newTtlCache<CappedRelayReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped relay reviews. Empty fields match all. */
data class CappedRelayReviewQuery(
    var locale: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedRelayReview): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedRelayReview>): MutableList<CappedRelayReview> {
        val matched = filter(items) { candidate: CappedRelayReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedRelayReviews(store: CappedRelayReviewStore, query: CappedRelayReviewQuery): MutableList<CappedRelayReview> {
    return query.apply(store.list())
}
