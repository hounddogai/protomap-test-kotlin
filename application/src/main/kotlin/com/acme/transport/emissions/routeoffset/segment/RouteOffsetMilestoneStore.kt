package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset milestones by code. */
interface RouteOffsetMilestoneStore {
    fun find(code: String): RouteOffsetMilestone?
    fun save(item: RouteOffsetMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetMilestoneStore(
    var items: MutableMap<String, RouteOffsetMilestone>,
    var order: MutableList<String>,
) : RouteOffsetMilestoneStore {
    override fun find(code: String): RouteOffsetMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetMilestone) {
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

    override fun list(): MutableList<RouteOffsetMilestone> {
        val out = mutableListOf<RouteOffsetMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetMilestoneStore(): RouteOffsetMilestoneStore {
    return MemoryRouteOffsetMilestoneStore(
        mutableMapOf<String, RouteOffsetMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetMilestoneStore(
    var inner: RouteOffsetMilestoneStore,
    var cache: TtlCache<RouteOffsetMilestone>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetMilestoneStore {
    override fun find(code: String): RouteOffsetMilestone? {
        val hit = (cache.get(code) as RouteOffsetMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetMilestone> = inner.list()
}

fun newCachedRouteOffsetMilestoneStore(inner: RouteOffsetMilestoneStore, ttl: Int, clock: Clock): RouteOffsetMilestoneStore {
    return CachedRouteOffsetMilestoneStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset milestones. Empty fields match all. */
data class RouteOffsetMilestoneQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetMilestone>): MutableList<RouteOffsetMilestone> {
        val matched = filter(items) { candidate: RouteOffsetMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetMilestones(store: RouteOffsetMilestoneStore, query: RouteOffsetMilestoneQuery): MutableList<RouteOffsetMilestone> {
    return query.apply(store.list())
}
