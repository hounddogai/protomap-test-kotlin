package com.acme.transport.emissions.routeoffset.rule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset segments by code. */
interface RouteOffsetSegmentStore {
    fun find(code: String): RouteOffsetSegment?
    fun save(item: RouteOffsetSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetSegmentStore(
    var items: MutableMap<String, RouteOffsetSegment>,
    var order: MutableList<String>,
) : RouteOffsetSegmentStore {
    override fun find(code: String): RouteOffsetSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetSegment) {
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

    override fun list(): MutableList<RouteOffsetSegment> {
        val out = mutableListOf<RouteOffsetSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetSegmentStore(): RouteOffsetSegmentStore {
    return MemoryRouteOffsetSegmentStore(mutableMapOf<String, RouteOffsetSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetSegmentStore(
    var inner: RouteOffsetSegmentStore,
    var cache: TtlCache<RouteOffsetSegment>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetSegmentStore {
    override fun find(code: String): RouteOffsetSegment? {
        val hit = (cache.get(code) as RouteOffsetSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetSegment> = inner.list()
}

fun newCachedRouteOffsetSegmentStore(inner: RouteOffsetSegmentStore, ttl: Int, clock: Clock): RouteOffsetSegmentStore {
    return CachedRouteOffsetSegmentStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetSegment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset segments. Empty fields match all. */
data class RouteOffsetSegmentQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetSegment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetSegment>): MutableList<RouteOffsetSegment> {
        val matched = filter(items) { candidate: RouteOffsetSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetSegments(store: RouteOffsetSegmentStore, query: RouteOffsetSegmentQuery): MutableList<RouteOffsetSegment> {
    return query.apply(store.list())
}
