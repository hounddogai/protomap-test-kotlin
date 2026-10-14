package com.acme.transport.terminal.crossdock.regionaldockclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted cross dock claims by code. */
interface WeightedCrossDockClaimStore {
    fun find(code: String): WeightedCrossDockClaim?
    fun save(item: WeightedCrossDockClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCrossDockClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCrossDockClaimStore(
    var items: MutableMap<String, WeightedCrossDockClaim>,
    var order: MutableList<String>,
) : WeightedCrossDockClaimStore {
    override fun find(code: String): WeightedCrossDockClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCrossDockClaim) {
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

    override fun list(): MutableList<WeightedCrossDockClaim> {
        val out = mutableListOf<WeightedCrossDockClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCrossDockClaimStore(): WeightedCrossDockClaimStore {
    return MemoryWeightedCrossDockClaimStore(
        mutableMapOf<String, WeightedCrossDockClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCrossDockClaimStore(
    var inner: WeightedCrossDockClaimStore,
    var cache: TtlCache<WeightedCrossDockClaim>,
    var hits: Int,
    var misses: Int,
) : WeightedCrossDockClaimStore {
    override fun find(code: String): WeightedCrossDockClaim? {
        val hit = (cache.get(code) as WeightedCrossDockClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCrossDockClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCrossDockClaim> = inner.list()
}

fun newCachedWeightedCrossDockClaimStore(inner: WeightedCrossDockClaimStore, ttl: Int, clock: Clock): WeightedCrossDockClaimStore {
    return CachedWeightedCrossDockClaimStore(
        inner = inner,
        cache = newTtlCache<WeightedCrossDockClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted cross dock claims. Empty fields match all. */
data class WeightedCrossDockClaimQuery(
    var channel: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCrossDockClaim): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedCrossDockClaim>): MutableList<WeightedCrossDockClaim> {
        val matched = filter(items) { candidate: WeightedCrossDockClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCrossDockClaims(store: WeightedCrossDockClaimStore, query: WeightedCrossDockClaimQuery): MutableList<WeightedCrossDockClaim> {
    return query.apply(store.list())
}
