package com.acme.transport.emissions.routeoffset.offsetmapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset digests by code. */
interface RouteOffsetDigestStore {
    fun find(code: String): RouteOffsetDigest?
    fun save(item: RouteOffsetDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetDigestStore(
    var items: MutableMap<String, RouteOffsetDigest>,
    var order: MutableList<String>,
) : RouteOffsetDigestStore {
    override fun find(code: String): RouteOffsetDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetDigest) {
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

    override fun list(): MutableList<RouteOffsetDigest> {
        val out = mutableListOf<RouteOffsetDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetDigestStore(): RouteOffsetDigestStore {
    return MemoryRouteOffsetDigestStore(mutableMapOf<String, RouteOffsetDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetDigestStore(
    var inner: RouteOffsetDigestStore,
    var cache: TtlCache<RouteOffsetDigest>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetDigestStore {
    override fun find(code: String): RouteOffsetDigest? {
        val hit = (cache.get(code) as RouteOffsetDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetDigest> = inner.list()
}

fun newCachedRouteOffsetDigestStore(inner: RouteOffsetDigestStore, ttl: Int, clock: Clock): RouteOffsetDigestStore {
    return CachedRouteOffsetDigestStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset digests. Empty fields match all. */
data class RouteOffsetDigestQuery(
    var label: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetDigest): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetDigest>): MutableList<RouteOffsetDigest> {
        val matched = filter(items) { candidate: RouteOffsetDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetDigests(store: RouteOffsetDigestStore, query: RouteOffsetDigestQuery): MutableList<RouteOffsetDigest> {
    return query.apply(store.list())
}
