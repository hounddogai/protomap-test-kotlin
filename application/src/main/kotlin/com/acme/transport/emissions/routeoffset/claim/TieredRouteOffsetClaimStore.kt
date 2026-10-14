package com.acme.transport.emissions.routeoffset.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered route offset claims by code. */
interface TieredRouteOffsetClaimStore {
    fun find(code: String): TieredRouteOffsetClaim?
    fun save(item: TieredRouteOffsetClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRouteOffsetClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRouteOffsetClaimStore(
    var items: MutableMap<String, TieredRouteOffsetClaim>,
    var order: MutableList<String>,
) : TieredRouteOffsetClaimStore {
    override fun find(code: String): TieredRouteOffsetClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRouteOffsetClaim) {
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

    override fun list(): MutableList<TieredRouteOffsetClaim> {
        val out = mutableListOf<TieredRouteOffsetClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRouteOffsetClaimStore(): TieredRouteOffsetClaimStore {
    return MemoryTieredRouteOffsetClaimStore(
        mutableMapOf<String, TieredRouteOffsetClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRouteOffsetClaimStore(
    var inner: TieredRouteOffsetClaimStore,
    var cache: TtlCache<TieredRouteOffsetClaim>,
    var hits: Int,
    var misses: Int,
) : TieredRouteOffsetClaimStore {
    override fun find(code: String): TieredRouteOffsetClaim? {
        val hit = (cache.get(code) as TieredRouteOffsetClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRouteOffsetClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRouteOffsetClaim> = inner.list()
}

fun newCachedTieredRouteOffsetClaimStore(inner: TieredRouteOffsetClaimStore, ttl: Int, clock: Clock): TieredRouteOffsetClaimStore {
    return CachedTieredRouteOffsetClaimStore(
        inner = inner,
        cache = newTtlCache<TieredRouteOffsetClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered route offset claims. Empty fields match all. */
data class TieredRouteOffsetClaimQuery(
    var segment: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRouteOffsetClaim): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRouteOffsetClaim>): MutableList<TieredRouteOffsetClaim> {
        val matched = filter(items) { candidate: TieredRouteOffsetClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRouteOffsetClaims(store: TieredRouteOffsetClaimStore, query: TieredRouteOffsetClaimQuery): MutableList<TieredRouteOffsetClaim> {
    return query.apply(store.list())
}
