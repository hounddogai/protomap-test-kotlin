package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted service area claims by code. */
interface WeightedServiceAreaClaimStore {
    fun find(code: String): WeightedServiceAreaClaim?
    fun save(item: WeightedServiceAreaClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedServiceAreaClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedServiceAreaClaimStore(
    var items: MutableMap<String, WeightedServiceAreaClaim>,
    var order: MutableList<String>,
) : WeightedServiceAreaClaimStore {
    override fun find(code: String): WeightedServiceAreaClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedServiceAreaClaim) {
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

    override fun list(): MutableList<WeightedServiceAreaClaim> {
        val out = mutableListOf<WeightedServiceAreaClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedServiceAreaClaimStore(): WeightedServiceAreaClaimStore {
    return MemoryWeightedServiceAreaClaimStore(
        mutableMapOf<String, WeightedServiceAreaClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedServiceAreaClaimStore(
    var inner: WeightedServiceAreaClaimStore,
    var cache: TtlCache<WeightedServiceAreaClaim>,
    var hits: Int,
    var misses: Int,
) : WeightedServiceAreaClaimStore {
    override fun find(code: String): WeightedServiceAreaClaim? {
        val hit = (cache.get(code) as WeightedServiceAreaClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedServiceAreaClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedServiceAreaClaim> = inner.list()
}

fun newCachedWeightedServiceAreaClaimStore(inner: WeightedServiceAreaClaimStore, ttl: Int, clock: Clock): WeightedServiceAreaClaimStore {
    return CachedWeightedServiceAreaClaimStore(
        inner = inner,
        cache = newTtlCache<WeightedServiceAreaClaim>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted service area claims. Empty fields match all. */
data class WeightedServiceAreaClaimQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedServiceAreaClaim): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedServiceAreaClaim>): MutableList<WeightedServiceAreaClaim> {
        val matched = filter(items) { candidate: WeightedServiceAreaClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedServiceAreaClaims(store: WeightedServiceAreaClaimStore, query: WeightedServiceAreaClaimQuery): MutableList<WeightedServiceAreaClaim> {
    return query.apply(store.list())
}
