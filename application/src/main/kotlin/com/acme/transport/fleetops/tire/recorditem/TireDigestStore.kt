package com.acme.transport.fleetops.tire.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire digests by code. */
interface TireDigestStore {
    fun find(code: String): TireDigest?
    fun save(item: TireDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireDigestStore(
    var items: MutableMap<String, TireDigest>,
    var order: MutableList<String>,
) : TireDigestStore {
    override fun find(code: String): TireDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireDigest) {
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

    override fun list(): MutableList<TireDigest> {
        val out = mutableListOf<TireDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireDigestStore(): TireDigestStore {
    return MemoryTireDigestStore(mutableMapOf<String, TireDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireDigestStore(
    var inner: TireDigestStore,
    var cache: TtlCache<TireDigest>,
    var hits: Int,
    var misses: Int,
) : TireDigestStore {
    override fun find(code: String): TireDigest? {
        val hit = (cache.get(code) as TireDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireDigest> = inner.list()
}

fun newCachedTireDigestStore(inner: TireDigestStore, ttl: Int, clock: Clock): TireDigestStore {
    return CachedTireDigestStore(
        inner = inner,
        cache = newTtlCache<TireDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire digests. Empty fields match all. */
data class TireDigestQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireDigest>): MutableList<TireDigest> {
        val matched = filter(items) { candidate: TireDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireDigests(store: TireDigestStore, query: TireDigestQuery): MutableList<TireDigest> {
    return query.apply(store.list())
}
