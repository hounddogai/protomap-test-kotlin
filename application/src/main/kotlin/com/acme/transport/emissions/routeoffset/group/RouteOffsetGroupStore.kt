package com.acme.transport.emissions.routeoffset.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset groups by code. */
interface RouteOffsetGroupStore {
    fun find(code: String): RouteOffsetGroup?
    fun save(item: RouteOffsetGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetGroupStore(
    var items: MutableMap<String, RouteOffsetGroup>,
    var order: MutableList<String>,
) : RouteOffsetGroupStore {
    override fun find(code: String): RouteOffsetGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetGroup) {
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

    override fun list(): MutableList<RouteOffsetGroup> {
        val out = mutableListOf<RouteOffsetGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetGroupStore(): RouteOffsetGroupStore {
    return MemoryRouteOffsetGroupStore(mutableMapOf<String, RouteOffsetGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetGroupStore(
    var inner: RouteOffsetGroupStore,
    var cache: TtlCache<RouteOffsetGroup>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetGroupStore {
    override fun find(code: String): RouteOffsetGroup? {
        val hit = (cache.get(code) as RouteOffsetGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetGroup> = inner.list()
}

fun newCachedRouteOffsetGroupStore(inner: RouteOffsetGroupStore, ttl: Int, clock: Clock): RouteOffsetGroupStore {
    return CachedRouteOffsetGroupStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset groups. Empty fields match all. */
data class RouteOffsetGroupQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetGroup>): MutableList<RouteOffsetGroup> {
        val matched = filter(items) { candidate: RouteOffsetGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetGroups(store: RouteOffsetGroupStore, query: RouteOffsetGroupQuery): MutableList<RouteOffsetGroup> {
    return query.apply(store.list())
}
