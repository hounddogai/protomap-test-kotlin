package com.acme.transport.fleetops.serviceplan.variance

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps service plan reviews by code. */
interface ServicePlanReviewStore {
    fun find(code: String): ServicePlanReview?
    fun save(item: ServicePlanReview)
    fun remove(code: String): Boolean
    fun list(): MutableList<ServicePlanReview>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryServicePlanReviewStore(
    var items: MutableMap<String, ServicePlanReview>,
    var order: MutableList<String>,
) : ServicePlanReviewStore {
    override fun find(code: String): ServicePlanReview? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ServicePlanReview) {
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

    override fun list(): MutableList<ServicePlanReview> {
        val out = mutableListOf<ServicePlanReview>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryServicePlanReviewStore(): ServicePlanReviewStore {
    return MemoryServicePlanReviewStore(mutableMapOf<String, ServicePlanReview>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedServicePlanReviewStore(
    var inner: ServicePlanReviewStore,
    var cache: TtlCache<ServicePlanReview>,
    var hits: Int,
    var misses: Int,
) : ServicePlanReviewStore {
    override fun find(code: String): ServicePlanReview? {
        val hit = (cache.get(code) as ServicePlanReview?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ServicePlanReview) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ServicePlanReview> = inner.list()
}

fun newCachedServicePlanReviewStore(inner: ServicePlanReviewStore, ttl: Int, clock: Clock): ServicePlanReviewStore {
    return CachedServicePlanReviewStore(
        inner = inner,
        cache = newTtlCache<ServicePlanReview>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects service plan reviews. Empty fields match all. */
data class ServicePlanReviewQuery(
    var region: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ServicePlanReview): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ServicePlanReview>): MutableList<ServicePlanReview> {
        val matched = filter(items) { candidate: ServicePlanReview -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findServicePlanReviews(store: ServicePlanReviewStore, query: ServicePlanReviewQuery): MutableList<ServicePlanReview> {
    return query.apply(store.list())
}
