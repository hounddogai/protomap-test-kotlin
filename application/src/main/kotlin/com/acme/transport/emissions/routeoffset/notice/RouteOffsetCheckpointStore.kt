package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset checkpoints by code. */
interface RouteOffsetCheckpointStore {
    fun find(code: String): RouteOffsetCheckpoint?
    fun save(item: RouteOffsetCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetCheckpointStore(
    var items: MutableMap<String, RouteOffsetCheckpoint>,
    var order: MutableList<String>,
) : RouteOffsetCheckpointStore {
    override fun find(code: String): RouteOffsetCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetCheckpoint) {
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

    override fun list(): MutableList<RouteOffsetCheckpoint> {
        val out = mutableListOf<RouteOffsetCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetCheckpointStore(): RouteOffsetCheckpointStore {
    return MemoryRouteOffsetCheckpointStore(
        mutableMapOf<String, RouteOffsetCheckpoint>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetCheckpointStore(
    var inner: RouteOffsetCheckpointStore,
    var cache: TtlCache<RouteOffsetCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetCheckpointStore {
    override fun find(code: String): RouteOffsetCheckpoint? {
        val hit = (cache.get(code) as RouteOffsetCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetCheckpoint> = inner.list()
}

fun newCachedRouteOffsetCheckpointStore(inner: RouteOffsetCheckpointStore, ttl: Int, clock: Clock): RouteOffsetCheckpointStore {
    return CachedRouteOffsetCheckpointStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetCheckpoint>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset checkpoints. Empty fields match all. */
data class RouteOffsetCheckpointQuery(
    var unit: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetCheckpoint): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetCheckpoint>): MutableList<RouteOffsetCheckpoint> {
        val matched = filter(items) { candidate: RouteOffsetCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetCheckpoints(store: RouteOffsetCheckpointStore, query: RouteOffsetCheckpointQuery): MutableList<RouteOffsetCheckpoint> {
    return query.apply(store.list())
}
