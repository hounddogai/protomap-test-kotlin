package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive route offset milestones by code. */
interface AdaptiveRouteOffsetMilestoneStore {
    fun find(code: String): AdaptiveRouteOffsetMilestone?
    fun save(item: AdaptiveRouteOffsetMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveRouteOffsetMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveRouteOffsetMilestoneStore(
    var items: MutableMap<String, AdaptiveRouteOffsetMilestone>,
    var order: MutableList<String>,
) : AdaptiveRouteOffsetMilestoneStore {
    override fun find(code: String): AdaptiveRouteOffsetMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveRouteOffsetMilestone) {
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

    override fun list(): MutableList<AdaptiveRouteOffsetMilestone> {
        val out = mutableListOf<AdaptiveRouteOffsetMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveRouteOffsetMilestoneStore(): AdaptiveRouteOffsetMilestoneStore {
    return MemoryAdaptiveRouteOffsetMilestoneStore(
        mutableMapOf<String, AdaptiveRouteOffsetMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveRouteOffsetMilestoneStore(
    var inner: AdaptiveRouteOffsetMilestoneStore,
    var cache: TtlCache<AdaptiveRouteOffsetMilestone>,
    var hits: Int,
    var misses: Int,
) : AdaptiveRouteOffsetMilestoneStore {
    override fun find(code: String): AdaptiveRouteOffsetMilestone? {
        val hit = (cache.get(code) as AdaptiveRouteOffsetMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveRouteOffsetMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveRouteOffsetMilestone> = inner.list()
}

fun newCachedAdaptiveRouteOffsetMilestoneStore(inner: AdaptiveRouteOffsetMilestoneStore, ttl: Int, clock: Clock): AdaptiveRouteOffsetMilestoneStore {
    return CachedAdaptiveRouteOffsetMilestoneStore(
        inner = inner,
        cache = newTtlCache<AdaptiveRouteOffsetMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive route offset milestones. Empty fields match all. */
data class AdaptiveRouteOffsetMilestoneQuery(
    var depot: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveRouteOffsetMilestone): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveRouteOffsetMilestone>): MutableList<AdaptiveRouteOffsetMilestone> {
        val matched = filter(items) { candidate: AdaptiveRouteOffsetMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveRouteOffsetMilestones(store: AdaptiveRouteOffsetMilestoneStore, query: AdaptiveRouteOffsetMilestoneQuery): MutableList<AdaptiveRouteOffsetMilestone> {
    return query.apply(store.list())
}
