package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset thresholds by code. */
interface RouteOffsetThresholdStore {
    fun find(code: String): RouteOffsetThreshold?
    fun save(item: RouteOffsetThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetThresholdStore(
    var items: MutableMap<String, RouteOffsetThreshold>,
    var order: MutableList<String>,
) : RouteOffsetThresholdStore {
    override fun find(code: String): RouteOffsetThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetThreshold) {
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

    override fun list(): MutableList<RouteOffsetThreshold> {
        val out = mutableListOf<RouteOffsetThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetThresholdStore(): RouteOffsetThresholdStore {
    return MemoryRouteOffsetThresholdStore(
        mutableMapOf<String, RouteOffsetThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetThresholdStore(
    var inner: RouteOffsetThresholdStore,
    var cache: TtlCache<RouteOffsetThreshold>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetThresholdStore {
    override fun find(code: String): RouteOffsetThreshold? {
        val hit = (cache.get(code) as RouteOffsetThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetThreshold> = inner.list()
}

fun newCachedRouteOffsetThresholdStore(inner: RouteOffsetThresholdStore, ttl: Int, clock: Clock): RouteOffsetThresholdStore {
    return CachedRouteOffsetThresholdStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset thresholds. Empty fields match all. */
data class RouteOffsetThresholdQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<RouteOffsetThreshold>): MutableList<RouteOffsetThreshold> {
        val matched = filter(items) { candidate: RouteOffsetThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetThresholds(store: RouteOffsetThresholdStore, query: RouteOffsetThresholdQuery): MutableList<RouteOffsetThreshold> {
    return query.apply(store.list())
}
