package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset adjustments by code. */
interface RouteOffsetAdjustmentStore {
    fun find(code: String): RouteOffsetAdjustment?
    fun save(item: RouteOffsetAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetAdjustmentStore(
    var items: MutableMap<String, RouteOffsetAdjustment>,
    var order: MutableList<String>,
) : RouteOffsetAdjustmentStore {
    override fun find(code: String): RouteOffsetAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetAdjustment) {
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

    override fun list(): MutableList<RouteOffsetAdjustment> {
        val out = mutableListOf<RouteOffsetAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetAdjustmentStore(): RouteOffsetAdjustmentStore {
    return MemoryRouteOffsetAdjustmentStore(
        mutableMapOf<String, RouteOffsetAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetAdjustmentStore(
    var inner: RouteOffsetAdjustmentStore,
    var cache: TtlCache<RouteOffsetAdjustment>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetAdjustmentStore {
    override fun find(code: String): RouteOffsetAdjustment? {
        val hit = (cache.get(code) as RouteOffsetAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetAdjustment> = inner.list()
}

fun newCachedRouteOffsetAdjustmentStore(inner: RouteOffsetAdjustmentStore, ttl: Int, clock: Clock): RouteOffsetAdjustmentStore {
    return CachedRouteOffsetAdjustmentStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset adjustments. Empty fields match all. */
data class RouteOffsetAdjustmentQuery(
    var title: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetAdjustment>): MutableList<RouteOffsetAdjustment> {
        val matched = filter(items) { candidate: RouteOffsetAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetAdjustments(store: RouteOffsetAdjustmentStore, query: RouteOffsetAdjustmentQuery): MutableList<RouteOffsetAdjustment> {
    return query.apply(store.list())
}
