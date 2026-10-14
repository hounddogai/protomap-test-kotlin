package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset plans by code. */
interface RouteOffsetPlanStore {
    fun find(code: String): RouteOffsetPlan?
    fun save(item: RouteOffsetPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetPlanStore(
    var items: MutableMap<String, RouteOffsetPlan>,
    var order: MutableList<String>,
) : RouteOffsetPlanStore {
    override fun find(code: String): RouteOffsetPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetPlan) {
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

    override fun list(): MutableList<RouteOffsetPlan> {
        val out = mutableListOf<RouteOffsetPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetPlanStore(): RouteOffsetPlanStore {
    return MemoryRouteOffsetPlanStore(mutableMapOf<String, RouteOffsetPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetPlanStore(
    var inner: RouteOffsetPlanStore,
    var cache: TtlCache<RouteOffsetPlan>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetPlanStore {
    override fun find(code: String): RouteOffsetPlan? {
        val hit = (cache.get(code) as RouteOffsetPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetPlan> = inner.list()
}

fun newCachedRouteOffsetPlanStore(inner: RouteOffsetPlanStore, ttl: Int, clock: Clock): RouteOffsetPlanStore {
    return CachedRouteOffsetPlanStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset plans. Empty fields match all. */
data class RouteOffsetPlanQuery(
    var channel: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetPlan): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetPlan>): MutableList<RouteOffsetPlan> {
        val matched = filter(items) { candidate: RouteOffsetPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetPlans(store: RouteOffsetPlanStore, query: RouteOffsetPlanQuery): MutableList<RouteOffsetPlan> {
    return query.apply(store.list())
}
