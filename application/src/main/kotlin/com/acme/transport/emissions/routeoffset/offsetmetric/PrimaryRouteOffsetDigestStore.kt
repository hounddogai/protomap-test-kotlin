package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary route offset digests by code. */
interface PrimaryRouteOffsetDigestStore {
    fun find(code: String): PrimaryRouteOffsetDigest?
    fun save(item: PrimaryRouteOffsetDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRouteOffsetDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRouteOffsetDigestStore(
    var items: MutableMap<String, PrimaryRouteOffsetDigest>,
    var order: MutableList<String>,
) : PrimaryRouteOffsetDigestStore {
    override fun find(code: String): PrimaryRouteOffsetDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRouteOffsetDigest) {
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

    override fun list(): MutableList<PrimaryRouteOffsetDigest> {
        val out = mutableListOf<PrimaryRouteOffsetDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRouteOffsetDigestStore(): PrimaryRouteOffsetDigestStore {
    return MemoryPrimaryRouteOffsetDigestStore(
        mutableMapOf<String, PrimaryRouteOffsetDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRouteOffsetDigestStore(
    var inner: PrimaryRouteOffsetDigestStore,
    var cache: TtlCache<PrimaryRouteOffsetDigest>,
    var hits: Int,
    var misses: Int,
) : PrimaryRouteOffsetDigestStore {
    override fun find(code: String): PrimaryRouteOffsetDigest? {
        val hit = (cache.get(code) as PrimaryRouteOffsetDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRouteOffsetDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRouteOffsetDigest> = inner.list()
}

fun newCachedPrimaryRouteOffsetDigestStore(inner: PrimaryRouteOffsetDigestStore, ttl: Int, clock: Clock): PrimaryRouteOffsetDigestStore {
    return CachedPrimaryRouteOffsetDigestStore(
        inner = inner,
        cache = newTtlCache<PrimaryRouteOffsetDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary route offset digests. Empty fields match all. */
data class PrimaryRouteOffsetDigestQuery(
    var name: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRouteOffsetDigest): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryRouteOffsetDigest>): MutableList<PrimaryRouteOffsetDigest> {
        val matched = filter(items) { candidate: PrimaryRouteOffsetDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRouteOffsetDigests(store: PrimaryRouteOffsetDigestStore, query: PrimaryRouteOffsetDigestQuery): MutableList<PrimaryRouteOffsetDigest> {
    return query.apply(store.list())
}
