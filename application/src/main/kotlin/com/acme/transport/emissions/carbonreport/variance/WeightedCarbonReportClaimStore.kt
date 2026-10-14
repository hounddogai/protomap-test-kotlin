package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted carbon report claims by code. */
interface WeightedCarbonReportClaimStore {
    fun find(code: String): WeightedCarbonReportClaim?
    fun save(item: WeightedCarbonReportClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCarbonReportClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCarbonReportClaimStore(
    var items: MutableMap<String, WeightedCarbonReportClaim>,
    var order: MutableList<String>,
) : WeightedCarbonReportClaimStore {
    override fun find(code: String): WeightedCarbonReportClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCarbonReportClaim) {
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

    override fun list(): MutableList<WeightedCarbonReportClaim> {
        val out = mutableListOf<WeightedCarbonReportClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCarbonReportClaimStore(): WeightedCarbonReportClaimStore {
    return MemoryWeightedCarbonReportClaimStore(
        mutableMapOf<String, WeightedCarbonReportClaim>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCarbonReportClaimStore(
    var inner: WeightedCarbonReportClaimStore,
    var cache: TtlCache<WeightedCarbonReportClaim>,
    var hits: Int,
    var misses: Int,
) : WeightedCarbonReportClaimStore {
    override fun find(code: String): WeightedCarbonReportClaim? {
        val hit = (cache.get(code) as WeightedCarbonReportClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCarbonReportClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCarbonReportClaim> = inner.list()
}

fun newCachedWeightedCarbonReportClaimStore(inner: WeightedCarbonReportClaimStore, ttl: Int, clock: Clock): WeightedCarbonReportClaimStore {
    return CachedWeightedCarbonReportClaimStore(
        inner = inner,
        cache = newTtlCache<WeightedCarbonReportClaim>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted carbon report claims. Empty fields match all. */
data class WeightedCarbonReportClaimQuery(
    var depot: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCarbonReportClaim): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<WeightedCarbonReportClaim>): MutableList<WeightedCarbonReportClaim> {
        val matched = filter(items) { candidate: WeightedCarbonReportClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCarbonReportClaims(store: WeightedCarbonReportClaimStore, query: WeightedCarbonReportClaimQuery): MutableList<WeightedCarbonReportClaim> {
    return query.apply(store.list())
}
