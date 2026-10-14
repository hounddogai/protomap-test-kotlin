package com.acme.transport.linehaul.relay.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps relay reviews by code. */
interface RelayReviewStore {
    fun find(code: String): RelayReview?
    fun save(item: RelayReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<RelayReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRelayReviewStore(
    var items: MutableMap<String, RelayReview>,
    var order: MutableList<String>,
) : RelayReviewStore {
    override fun find(code: String): RelayReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RelayReview) {
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

    override fun list(): MutableList<RelayReview> {
        val out = mutableListOf<RelayReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRelayReviewStore(): RelayReviewStore {
    return MemoryRelayReviewStore(mutableMapOf<String, RelayReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRelayReviewStore(
    var inner: RelayReviewStore,
    var cache: TtlCache<RelayReview>,
    var hits: Int,
    var misses: Int,
) : RelayReviewStore {
    override fun find(code: String): RelayReview? {
        val hit = (cache.get(code) as RelayReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RelayReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RelayReview> = inner.list()
}

fun newCachedRelayReviewStore(inner: RelayReviewStore, ttl: Int, clock: Clock): RelayReviewStore {
    return CachedRelayReviewStore(
        inner = inner,
        cache = newTtlCache<RelayReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects relay reviews. Empty fields match all. */
data class RelayReviewQuery(
    var region: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RelayReview): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<RelayReview>): MutableList<RelayReview> {
        val matched = filter(items) { candidate: RelayReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRelayReviews(store: RelayReviewStore, query: RelayReviewQuery): MutableList<RelayReview> {
    return query.apply(store.list())
}
