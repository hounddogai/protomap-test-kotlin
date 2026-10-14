package com.acme.transport.emissions.routeoffset.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset snapshots by code. */
interface RouteOffsetSnapshotStore {
    fun find(code: String): RouteOffsetSnapshot?
    fun save(item: RouteOffsetSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetSnapshotStore(
    var items: MutableMap<String, RouteOffsetSnapshot>,
    var order: MutableList<String>,
) : RouteOffsetSnapshotStore {
    override fun find(code: String): RouteOffsetSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetSnapshot) {
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

    override fun list(): MutableList<RouteOffsetSnapshot> {
        val out = mutableListOf<RouteOffsetSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetSnapshotStore(): RouteOffsetSnapshotStore {
    return MemoryRouteOffsetSnapshotStore(mutableMapOf<String, RouteOffsetSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetSnapshotStore(
    var inner: RouteOffsetSnapshotStore,
    var cache: TtlCache<RouteOffsetSnapshot>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetSnapshotStore {
    override fun find(code: String): RouteOffsetSnapshot? {
        val hit = (cache.get(code) as RouteOffsetSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetSnapshot> = inner.list()
}

fun newCachedRouteOffsetSnapshotStore(inner: RouteOffsetSnapshotStore, ttl: Int, clock: Clock): RouteOffsetSnapshotStore {
    return CachedRouteOffsetSnapshotStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset snapshots. Empty fields match all. */
data class RouteOffsetSnapshotQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetSnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetSnapshot>): MutableList<RouteOffsetSnapshot> {
        val matched = filter(items) { candidate: RouteOffsetSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetSnapshots(store: RouteOffsetSnapshotStore, query: RouteOffsetSnapshotQuery): MutableList<RouteOffsetSnapshot> {
    return query.apply(store.list())
}
