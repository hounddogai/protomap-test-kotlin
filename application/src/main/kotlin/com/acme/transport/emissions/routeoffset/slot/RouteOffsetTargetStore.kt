package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset targets by code. */
interface RouteOffsetTargetStore {
    fun find(code: String): RouteOffsetTarget?
    fun save(item: RouteOffsetTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetTargetStore(
    var items: MutableMap<String, RouteOffsetTarget>,
    var order: MutableList<String>,
) : RouteOffsetTargetStore {
    override fun find(code: String): RouteOffsetTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetTarget) {
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

    override fun list(): MutableList<RouteOffsetTarget> {
        val out = mutableListOf<RouteOffsetTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetTargetStore(): RouteOffsetTargetStore {
    return MemoryRouteOffsetTargetStore(mutableMapOf<String, RouteOffsetTarget>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetTargetStore(
    var inner: RouteOffsetTargetStore,
    var cache: TtlCache<RouteOffsetTarget>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetTargetStore {
    override fun find(code: String): RouteOffsetTarget? {
        val hit = (cache.get(code) as RouteOffsetTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetTarget> = inner.list()
}

fun newCachedRouteOffsetTargetStore(inner: RouteOffsetTargetStore, ttl: Int, clock: Clock): RouteOffsetTargetStore {
    return CachedRouteOffsetTargetStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetTarget>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset targets. Empty fields match all. */
data class RouteOffsetTargetQuery(
    var source: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetTarget): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<RouteOffsetTarget>): MutableList<RouteOffsetTarget> {
        val matched = filter(items) { candidate: RouteOffsetTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetTargets(store: RouteOffsetTargetStore, query: RouteOffsetTargetQuery): MutableList<RouteOffsetTarget> {
    return query.apply(store.list())
}
