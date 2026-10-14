package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending route offset digests by code. */
interface PendingRouteOffsetDigestStore {
    fun find(code: String): PendingRouteOffsetDigest?
    fun save(item: PendingRouteOffsetDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingRouteOffsetDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingRouteOffsetDigestStore(
    var items: MutableMap<String, PendingRouteOffsetDigest>,
    var order: MutableList<String>,
) : PendingRouteOffsetDigestStore {
    override fun find(code: String): PendingRouteOffsetDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingRouteOffsetDigest) {
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

    override fun list(): MutableList<PendingRouteOffsetDigest> {
        val out = mutableListOf<PendingRouteOffsetDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingRouteOffsetDigestStore(): PendingRouteOffsetDigestStore {
    return MemoryPendingRouteOffsetDigestStore(
        mutableMapOf<String, PendingRouteOffsetDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingRouteOffsetDigestStore(
    var inner: PendingRouteOffsetDigestStore,
    var cache: TtlCache<PendingRouteOffsetDigest>,
    var hits: Int,
    var misses: Int,
) : PendingRouteOffsetDigestStore {
    override fun find(code: String): PendingRouteOffsetDigest? {
        val hit = (cache.get(code) as PendingRouteOffsetDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingRouteOffsetDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingRouteOffsetDigest> = inner.list()
}

fun newCachedPendingRouteOffsetDigestStore(inner: PendingRouteOffsetDigestStore, ttl: Int, clock: Clock): PendingRouteOffsetDigestStore {
    return CachedPendingRouteOffsetDigestStore(
        inner = inner,
        cache = newTtlCache<PendingRouteOffsetDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending route offset digests. Empty fields match all. */
data class PendingRouteOffsetDigestQuery(
    var title: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingRouteOffsetDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingRouteOffsetDigest>): MutableList<PendingRouteOffsetDigest> {
        val matched = filter(items) { candidate: PendingRouteOffsetDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingRouteOffsetDigests(store: PendingRouteOffsetDigestStore, query: PendingRouteOffsetDigestQuery): MutableList<PendingRouteOffsetDigest> {
    return query.apply(store.list())
}
