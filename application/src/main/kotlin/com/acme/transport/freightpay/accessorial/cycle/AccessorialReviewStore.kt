package com.acme.transport.freightpay.accessorial.cycle

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps accessorial reviews by code. */
interface AccessorialReviewStore {
    fun find(code: String): AccessorialReview?
    fun save(item: AccessorialReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<AccessorialReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAccessorialReviewStore(
    var items: MutableMap<String, AccessorialReview>,
    var order: MutableList<String>,
) : AccessorialReviewStore {
    override fun find(code: String): AccessorialReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AccessorialReview) {
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

    override fun list(): MutableList<AccessorialReview> {
        val out = mutableListOf<AccessorialReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAccessorialReviewStore(): AccessorialReviewStore {
    return MemoryAccessorialReviewStore(mutableMapOf<String, AccessorialReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAccessorialReviewStore(
    var inner: AccessorialReviewStore,
    var cache: TtlCache<AccessorialReview>,
    var hits: Int,
    var misses: Int,
) : AccessorialReviewStore {
    override fun find(code: String): AccessorialReview? {
        val hit = (cache.get(code) as AccessorialReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AccessorialReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AccessorialReview> = inner.list()
}

fun newCachedAccessorialReviewStore(inner: AccessorialReviewStore, ttl: Int, clock: Clock): AccessorialReviewStore {
    return CachedAccessorialReviewStore(
        inner = inner,
        cache = newTtlCache<AccessorialReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects accessorial reviews. Empty fields match all. */
data class AccessorialReviewQuery(
    var title: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AccessorialReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AccessorialReview>): MutableList<AccessorialReview> {
        val matched = filter(items) { candidate: AccessorialReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAccessorialReviews(store: AccessorialReviewStore, query: AccessorialReviewQuery): MutableList<AccessorialReview> {
    return query.apply(store.list())
}
