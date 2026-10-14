package com.acme.transport.safety.nearmiss.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps near miss reviews by code. */
interface NearMissReviewStore {
    fun find(code: String): NearMissReview?
    fun save(item: NearMissReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<NearMissReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryNearMissReviewStore(
    var items: MutableMap<String, NearMissReview>,
    var order: MutableList<String>,
) : NearMissReviewStore {
    override fun find(code: String): NearMissReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: NearMissReview) {
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

    override fun list(): MutableList<NearMissReview> {
        val out = mutableListOf<NearMissReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryNearMissReviewStore(): NearMissReviewStore {
    return MemoryNearMissReviewStore(mutableMapOf<String, NearMissReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedNearMissReviewStore(
    var inner: NearMissReviewStore,
    var cache: TtlCache<NearMissReview>,
    var hits: Int,
    var misses: Int,
) : NearMissReviewStore {
    override fun find(code: String): NearMissReview? {
        val hit = (cache.get(code) as NearMissReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: NearMissReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<NearMissReview> = inner.list()
}

fun newCachedNearMissReviewStore(inner: NearMissReviewStore, ttl: Int, clock: Clock): NearMissReviewStore {
    return CachedNearMissReviewStore(
        inner = inner,
        cache = newTtlCache<NearMissReview>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects near miss reviews. Empty fields match all. */
data class NearMissReviewQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: NearMissReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<NearMissReview>): MutableList<NearMissReview> {
        val matched = filter(items) { candidate: NearMissReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findNearMissReviews(store: NearMissReviewStore, query: NearMissReviewQuery): MutableList<NearMissReview> {
    return query.apply(store.list())
}
