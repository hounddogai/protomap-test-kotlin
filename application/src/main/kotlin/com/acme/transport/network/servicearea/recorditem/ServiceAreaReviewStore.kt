package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps service area reviews by code. */
interface ServiceAreaReviewStore {
    fun find(code: String): ServiceAreaReview?
    fun save(item: ServiceAreaReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServiceAreaReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServiceAreaReviewStore(
    var items: MutableMap<String, ServiceAreaReview>,
    var order: MutableList<String>,
) : ServiceAreaReviewStore {
    override fun find(code: String): ServiceAreaReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServiceAreaReview) {
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

    override fun list(): MutableList<ServiceAreaReview> {
        val out = mutableListOf<ServiceAreaReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServiceAreaReviewStore(): ServiceAreaReviewStore {
    return MemoryServiceAreaReviewStore(mutableMapOf<String, ServiceAreaReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServiceAreaReviewStore(
    var inner: ServiceAreaReviewStore,
    var cache: TtlCache<ServiceAreaReview>,
    var hits: Int,
    var misses: Int,
) : ServiceAreaReviewStore {
    override fun find(code: String): ServiceAreaReview? {
        val hit = (cache.get(code) as ServiceAreaReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServiceAreaReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServiceAreaReview> = inner.list()
}

fun newCachedServiceAreaReviewStore(inner: ServiceAreaReviewStore, ttl: Int, clock: Clock): ServiceAreaReviewStore {
    return CachedServiceAreaReviewStore(
        inner = inner,
        cache = newTtlCache<ServiceAreaReview>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service area reviews. Empty fields match all. */
data class ServiceAreaReviewQuery(
    var title: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServiceAreaReview): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServiceAreaReview>): MutableList<ServiceAreaReview> {
        val matched = filter(items) { candidate: ServiceAreaReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServiceAreaReviews(store: ServiceAreaReviewStore, query: ServiceAreaReviewQuery): MutableList<ServiceAreaReview> {
    return query.apply(store.list())
}
