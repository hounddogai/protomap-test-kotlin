package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report variances by code. */
interface CarbonReportVarianceStore {
    fun find(code: String): CarbonReportVariance?
    fun save(item: CarbonReportVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportVarianceStore(
    var items: MutableMap<String, CarbonReportVariance>,
    var order: MutableList<String>,
) : CarbonReportVarianceStore {
    override fun find(code: String): CarbonReportVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportVariance) {
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

    override fun list(): MutableList<CarbonReportVariance> {
        val out = mutableListOf<CarbonReportVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportVarianceStore(): CarbonReportVarianceStore {
    return MemoryCarbonReportVarianceStore(
        mutableMapOf<String, CarbonReportVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportVarianceStore(
    var inner: CarbonReportVarianceStore,
    var cache: TtlCache<CarbonReportVariance>,
    var hits: Int,
    var misses: Int,
) : CarbonReportVarianceStore {
    override fun find(code: String): CarbonReportVariance? {
        val hit = (cache.get(code) as CarbonReportVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportVariance> = inner.list()
}

fun newCachedCarbonReportVarianceStore(inner: CarbonReportVarianceStore, ttl: Int, clock: Clock): CarbonReportVarianceStore {
    return CachedCarbonReportVarianceStore(
        inner = inner,
        cache = newTtlCache<CarbonReportVariance>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report variances. Empty fields match all. */
data class CarbonReportVarianceQuery(
    var label: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportVariance): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportVariance>): MutableList<CarbonReportVariance> {
        val matched = filter(items) { candidate: CarbonReportVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportVariances(store: CarbonReportVarianceStore, query: CarbonReportVarianceQuery): MutableList<CarbonReportVariance> {
    return query.apply(store.list())
}
