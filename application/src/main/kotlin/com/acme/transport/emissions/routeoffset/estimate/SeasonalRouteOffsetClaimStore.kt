package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal route offset claims by code. */
interface SeasonalRouteOffsetClaimStore {
    fun find(code: String): SeasonalRouteOffsetClaim?
    fun save(item: SeasonalRouteOffsetClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalRouteOffsetClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalRouteOffsetClaimStore(
    var items: MutableMap<String, SeasonalRouteOffsetClaim>,
    var order: MutableList<String>,
) : SeasonalRouteOffsetClaimStore {
    override fun find(code: String): SeasonalRouteOffsetClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalRouteOffsetClaim) {
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

    override fun list(): MutableList<SeasonalRouteOffsetClaim> {
        val out = mutableListOf<SeasonalRouteOffsetClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalRouteOffsetClaimStore(): SeasonalRouteOffsetClaimStore {
    return MemorySeasonalRouteOffsetClaimStore(
        mutableMapOf<String, SeasonalRouteOffsetClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalRouteOffsetClaimStore(
    var inner: SeasonalRouteOffsetClaimStore,
    var cache: TtlCache<SeasonalRouteOffsetClaim>,
    var hits: Int,
    var misses: Int,
) : SeasonalRouteOffsetClaimStore {
    override fun find(code: String): SeasonalRouteOffsetClaim? {
        val hit = (cache.get(code) as SeasonalRouteOffsetClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalRouteOffsetClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalRouteOffsetClaim> = inner.list()
}

fun newCachedSeasonalRouteOffsetClaimStore(inner: SeasonalRouteOffsetClaimStore, ttl: Int, clock: Clock): SeasonalRouteOffsetClaimStore {
    return CachedSeasonalRouteOffsetClaimStore(
        inner = inner,
        cache = newTtlCache<SeasonalRouteOffsetClaim>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal route offset claims. Empty fields match all. */
data class SeasonalRouteOffsetClaimQuery(
    var depot: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalRouteOffsetClaim): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalRouteOffsetClaim>): MutableList<SeasonalRouteOffsetClaim> {
        val matched = filter(items) { candidate: SeasonalRouteOffsetClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalRouteOffsetClaims(store: SeasonalRouteOffsetClaimStore, query: SeasonalRouteOffsetClaimQuery): MutableList<SeasonalRouteOffsetClaim> {
    return query.apply(store.list())
}
