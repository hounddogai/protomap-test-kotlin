package com.acme.transport.safety.incident.incidentsegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident reviews by code. */
interface IncidentReviewStore {
    fun find(code: String): IncidentReview?
    fun save(item: IncidentReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentReviewStore(
    var items: MutableMap<String, IncidentReview>,
    var order: MutableList<String>,
) : IncidentReviewStore {
    override fun find(code: String): IncidentReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentReview) {
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

    override fun list(): MutableList<IncidentReview> {
        val out = mutableListOf<IncidentReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentReviewStore(): IncidentReviewStore {
    return MemoryIncidentReviewStore(mutableMapOf<String, IncidentReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentReviewStore(
    var inner: IncidentReviewStore,
    var cache: TtlCache<IncidentReview>,
    var hits: Int,
    var misses: Int,
) : IncidentReviewStore {
    override fun find(code: String): IncidentReview? {
        val hit = (cache.get(code) as IncidentReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentReview> = inner.list()
}

fun newCachedIncidentReviewStore(inner: IncidentReviewStore, ttl: Int, clock: Clock): IncidentReviewStore {
    return CachedIncidentReviewStore(
        inner = inner,
        cache = newTtlCache<IncidentReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident reviews. Empty fields match all. */
data class IncidentReviewQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentReview): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<IncidentReview>): MutableList<IncidentReview> {
        val matched = filter(items) { candidate: IncidentReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentReviews(store: IncidentReviewStore, query: IncidentReviewQuery): MutableList<IncidentReview> {
    return query.apply(store.list())
}
