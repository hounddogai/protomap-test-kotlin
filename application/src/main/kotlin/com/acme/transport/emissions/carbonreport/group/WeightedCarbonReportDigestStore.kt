package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted carbon report digests by code. */
interface WeightedCarbonReportDigestStore {
    fun find(code: String): WeightedCarbonReportDigest?
    fun save(item: WeightedCarbonReportDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCarbonReportDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCarbonReportDigestStore(
    var items: MutableMap<String, WeightedCarbonReportDigest>,
    var order: MutableList<String>,
) : WeightedCarbonReportDigestStore {
    override fun find(code: String): WeightedCarbonReportDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCarbonReportDigest) {
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

    override fun list(): MutableList<WeightedCarbonReportDigest> {
        val out = mutableListOf<WeightedCarbonReportDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCarbonReportDigestStore(): WeightedCarbonReportDigestStore {
    return MemoryWeightedCarbonReportDigestStore(
        mutableMapOf<String, WeightedCarbonReportDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCarbonReportDigestStore(
    var inner: WeightedCarbonReportDigestStore,
    var cache: TtlCache<WeightedCarbonReportDigest>,
    var hits: Int,
    var misses: Int,
) : WeightedCarbonReportDigestStore {
    override fun find(code: String): WeightedCarbonReportDigest? {
        val hit = (cache.get(code) as WeightedCarbonReportDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCarbonReportDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCarbonReportDigest> = inner.list()
}

fun newCachedWeightedCarbonReportDigestStore(inner: WeightedCarbonReportDigestStore, ttl: Int, clock: Clock): WeightedCarbonReportDigestStore {
    return CachedWeightedCarbonReportDigestStore(
        inner = inner,
        cache = newTtlCache<WeightedCarbonReportDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted carbon report digests. Empty fields match all. */
data class WeightedCarbonReportDigestQuery(
    var unit: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCarbonReportDigest): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedCarbonReportDigest>): MutableList<WeightedCarbonReportDigest> {
        val matched = filter(items) { candidate: WeightedCarbonReportDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCarbonReportDigests(store: WeightedCarbonReportDigestStore, query: WeightedCarbonReportDigestQuery): MutableList<WeightedCarbonReportDigest> {
    return query.apply(store.list())
}
