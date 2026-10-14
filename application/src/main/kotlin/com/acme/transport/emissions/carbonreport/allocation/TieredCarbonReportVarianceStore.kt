package com.acme.transport.emissions.carbonreport.allocation

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered carbon report variances by code. */
interface TieredCarbonReportVarianceStore {
    fun find(code: String): TieredCarbonReportVariance?
    fun save(item: TieredCarbonReportVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCarbonReportVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCarbonReportVarianceStore(
    var items: MutableMap<String, TieredCarbonReportVariance>,
    var order: MutableList<String>,
) : TieredCarbonReportVarianceStore {
    override fun find(code: String): TieredCarbonReportVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCarbonReportVariance) {
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

    override fun list(): MutableList<TieredCarbonReportVariance> {
        val out = mutableListOf<TieredCarbonReportVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCarbonReportVarianceStore(): TieredCarbonReportVarianceStore {
    return MemoryTieredCarbonReportVarianceStore(
        mutableMapOf<String, TieredCarbonReportVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCarbonReportVarianceStore(
    var inner: TieredCarbonReportVarianceStore,
    var cache: TtlCache<TieredCarbonReportVariance>,
    var hits: Int,
    var misses: Int,
) : TieredCarbonReportVarianceStore {
    override fun find(code: String): TieredCarbonReportVariance? {
        val hit = (cache.get(code) as TieredCarbonReportVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCarbonReportVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCarbonReportVariance> = inner.list()
}

fun newCachedTieredCarbonReportVarianceStore(inner: TieredCarbonReportVarianceStore, ttl: Int, clock: Clock): TieredCarbonReportVarianceStore {
    return CachedTieredCarbonReportVarianceStore(
        inner = inner,
        cache = newTtlCache<TieredCarbonReportVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered carbon report variances. Empty fields match all. */
data class TieredCarbonReportVarianceQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCarbonReportVariance): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCarbonReportVariance>): MutableList<TieredCarbonReportVariance> {
        val matched = filter(items) { candidate: TieredCarbonReportVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCarbonReportVariances(store: TieredCarbonReportVarianceStore, query: TieredCarbonReportVarianceQuery): MutableList<TieredCarbonReportVariance> {
    return query.apply(store.list())
}
