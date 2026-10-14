package com.acme.transport.safety.incident.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional incident reviews by code. */
interface RegionalIncidentReviewStore {
    fun find(code: String): RegionalIncidentReview?
    fun save(item: RegionalIncidentReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalIncidentReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalIncidentReviewStore(
    var items: MutableMap<String, RegionalIncidentReview>,
    var order: MutableList<String>,
) : RegionalIncidentReviewStore {
    override fun find(code: String): RegionalIncidentReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalIncidentReview) {
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

    override fun list(): MutableList<RegionalIncidentReview> {
        val out = mutableListOf<RegionalIncidentReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalIncidentReviewStore(): RegionalIncidentReviewStore {
    return MemoryRegionalIncidentReviewStore(
        mutableMapOf<String, RegionalIncidentReview>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalIncidentReviewStore(
    var inner: RegionalIncidentReviewStore,
    var cache: TtlCache<RegionalIncidentReview>,
    var hits: Int,
    var misses: Int,
) : RegionalIncidentReviewStore {
    override fun find(code: String): RegionalIncidentReview? {
        val hit = (cache.get(code) as RegionalIncidentReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalIncidentReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalIncidentReview> = inner.list()
}

fun newCachedRegionalIncidentReviewStore(inner: RegionalIncidentReviewStore, ttl: Int, clock: Clock): RegionalIncidentReviewStore {
    return CachedRegionalIncidentReviewStore(
        inner = inner,
        cache = newTtlCache<RegionalIncidentReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional incident reviews. Empty fields match all. */
data class RegionalIncidentReviewQuery(
    var name: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalIncidentReview): Boolean {
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

    fun apply(items: MutableList<RegionalIncidentReview>): MutableList<RegionalIncidentReview> {
        val matched = filter(items) { candidate: RegionalIncidentReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalIncidentReviews(store: RegionalIncidentReviewStore, query: RegionalIncidentReviewQuery): MutableList<RegionalIncidentReview> {
    return query.apply(store.list())
}
