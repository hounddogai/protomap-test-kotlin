package com.acme.transport.emissions.evcharge.chargeclaim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge reviews by code. */
interface EvChargeReviewStore {
    fun find(code: String): EvChargeReview?
    fun save(item: EvChargeReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeReviewStore(
    var items: MutableMap<String, EvChargeReview>,
    var order: MutableList<String>,
) : EvChargeReviewStore {
    override fun find(code: String): EvChargeReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeReview) {
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

    override fun list(): MutableList<EvChargeReview> {
        val out = mutableListOf<EvChargeReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeReviewStore(): EvChargeReviewStore {
    return MemoryEvChargeReviewStore(mutableMapOf<String, EvChargeReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeReviewStore(
    var inner: EvChargeReviewStore,
    var cache: TtlCache<EvChargeReview>,
    var hits: Int,
    var misses: Int,
) : EvChargeReviewStore {
    override fun find(code: String): EvChargeReview? {
        val hit = (cache.get(code) as EvChargeReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeReview> = inner.list()
}

fun newCachedEvChargeReviewStore(inner: EvChargeReviewStore, ttl: Int, clock: Clock): EvChargeReviewStore {
    return CachedEvChargeReviewStore(
        inner = inner,
        cache = newTtlCache<EvChargeReview>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge reviews. Empty fields match all. */
data class EvChargeReviewQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeReview>): MutableList<EvChargeReview> {
        val matched = filter(items) { candidate: EvChargeReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeReviews(store: EvChargeReviewStore, query: EvChargeReviewQuery): MutableList<EvChargeReview> {
    return query.apply(store.list())
}
