package com.acme.transport.network.zone.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone requests by code. */
interface ZoneRequestStore {
    fun find(code: String): ZoneRequest?
    fun save(item: ZoneRequest)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneRequest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneRequestStore(
    var items: MutableMap<String, ZoneRequest>,
    var order: MutableList<String>,
) : ZoneRequestStore {
    override fun find(code: String): ZoneRequest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneRequest) {
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

    override fun list(): MutableList<ZoneRequest> {
        val out = mutableListOf<ZoneRequest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneRequestStore(): ZoneRequestStore {
    return MemoryZoneRequestStore(mutableMapOf<String, ZoneRequest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneRequestStore(
    var inner: ZoneRequestStore,
    var cache: TtlCache<ZoneRequest>,
    var hits: Int,
    var misses: Int,
) : ZoneRequestStore {
    override fun find(code: String): ZoneRequest? {
        val hit = (cache.get(code) as ZoneRequest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneRequest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneRequest> = inner.list()
}

fun newCachedZoneRequestStore(inner: ZoneRequestStore, ttl: Int, clock: Clock): ZoneRequestStore {
    return CachedZoneRequestStore(
        inner = inner,
        cache = newTtlCache<ZoneRequest>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone requests. Empty fields match all. */
data class ZoneRequestQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneRequest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneRequest>): MutableList<ZoneRequest> {
        val matched = filter(items) { candidate: ZoneRequest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneRequests(store: ZoneRequestStore, query: ZoneRequestQuery): MutableList<ZoneRequest> {
    return query.apply(store.list())
}
