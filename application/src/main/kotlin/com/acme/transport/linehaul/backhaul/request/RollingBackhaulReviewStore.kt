package com.acme.transport.linehaul.backhaul.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps rolling backhaul reviews by code. */
interface RollingBackhaulReviewStore {
    fun find(code: String): RollingBackhaulReview?
    fun save(item: RollingBackhaulReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingBackhaulReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingBackhaulReviewStore(
    var items: MutableMap<String, RollingBackhaulReview>,
    var order: MutableList<String>,
) : RollingBackhaulReviewStore {
    override fun find(code: String): RollingBackhaulReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingBackhaulReview) {
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

    override fun list(): MutableList<RollingBackhaulReview> {
        val out = mutableListOf<RollingBackhaulReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingBackhaulReviewStore(): RollingBackhaulReviewStore {
    return MemoryRollingBackhaulReviewStore(
        mutableMapOf<String, RollingBackhaulReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingBackhaulReviewStore(
    var inner: RollingBackhaulReviewStore,
    var cache: TtlCache<RollingBackhaulReview>,
    var hits: Int,
    var misses: Int,
) : RollingBackhaulReviewStore {
    override fun find(code: String): RollingBackhaulReview? {
        val hit = (cache.get(code) as RollingBackhaulReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingBackhaulReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingBackhaulReview> = inner.list()
}

fun newCachedRollingBackhaulReviewStore(inner: RollingBackhaulReviewStore, ttl: Int, clock: Clock): RollingBackhaulReviewStore {
    return CachedRollingBackhaulReviewStore(
        inner = inner,
        cache = newTtlCache<RollingBackhaulReview>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling backhaul reviews. Empty fields match all. */
data class RollingBackhaulReviewQuery(
    var category: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingBackhaulReview): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingBackhaulReview>): MutableList<RollingBackhaulReview> {
        val matched = filter(items) { candidate: RollingBackhaulReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingBackhaulReviews(store: RollingBackhaulReviewStore, query: RollingBackhaulReviewQuery): MutableList<RollingBackhaulReview> {
    return query.apply(store.list())
}
