package com.acme.transport.emissions.routeoffset.overrideitem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset overrides by code. */
interface RouteOffsetOverrideStore {
    fun find(code: String): RouteOffsetOverride?
    fun save(item: RouteOffsetOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetOverrideStore(
    var items: MutableMap<String, RouteOffsetOverride>,
    var order: MutableList<String>,
) : RouteOffsetOverrideStore {
    override fun find(code: String): RouteOffsetOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetOverride) {
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

    override fun list(): MutableList<RouteOffsetOverride> {
        val out = mutableListOf<RouteOffsetOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetOverrideStore(): RouteOffsetOverrideStore {
    return MemoryRouteOffsetOverrideStore(mutableMapOf<String, RouteOffsetOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetOverrideStore(
    var inner: RouteOffsetOverrideStore,
    var cache: TtlCache<RouteOffsetOverride>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetOverrideStore {
    override fun find(code: String): RouteOffsetOverride? {
        val hit = (cache.get(code) as RouteOffsetOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetOverride> = inner.list()
}

fun newCachedRouteOffsetOverrideStore(inner: RouteOffsetOverrideStore, ttl: Int, clock: Clock): RouteOffsetOverrideStore {
    return CachedRouteOffsetOverrideStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetOverride>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset overrides. Empty fields match all. */
data class RouteOffsetOverrideQuery(
    var title: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetOverride): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetOverride>): MutableList<RouteOffsetOverride> {
        val matched = filter(items) { candidate: RouteOffsetOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetOverrides(store: RouteOffsetOverrideStore, query: RouteOffsetOverrideQuery): MutableList<RouteOffsetOverride> {
    return query.apply(store.list())
}
