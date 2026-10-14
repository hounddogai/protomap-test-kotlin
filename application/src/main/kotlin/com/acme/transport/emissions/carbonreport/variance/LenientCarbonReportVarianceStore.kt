package com.acme.transport.emissions.carbonreport.variance

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient carbon report variances by code. */
interface LenientCarbonReportVarianceStore {
    fun find(code: String): LenientCarbonReportVariance?
    fun save(item: LenientCarbonReportVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCarbonReportVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCarbonReportVarianceStore(
    var items: MutableMap<String, LenientCarbonReportVariance>,
    var order: MutableList<String>,
) : LenientCarbonReportVarianceStore {
    override fun find(code: String): LenientCarbonReportVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCarbonReportVariance) {
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

    override fun list(): MutableList<LenientCarbonReportVariance> {
        val out = mutableListOf<LenientCarbonReportVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCarbonReportVarianceStore(): LenientCarbonReportVarianceStore {
    return MemoryLenientCarbonReportVarianceStore(
        mutableMapOf<String, LenientCarbonReportVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCarbonReportVarianceStore(
    var inner: LenientCarbonReportVarianceStore,
    var cache: TtlCache<LenientCarbonReportVariance>,
    var hits: Int,
    var misses: Int,
) : LenientCarbonReportVarianceStore {
    override fun find(code: String): LenientCarbonReportVariance? {
        val hit = (cache.get(code) as LenientCarbonReportVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCarbonReportVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCarbonReportVariance> = inner.list()
}

fun newCachedLenientCarbonReportVarianceStore(inner: LenientCarbonReportVarianceStore, ttl: Int, clock: Clock): LenientCarbonReportVarianceStore {
    return CachedLenientCarbonReportVarianceStore(
        inner = inner,
        cache = newTtlCache<LenientCarbonReportVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient carbon report variances. Empty fields match all. */
data class LenientCarbonReportVarianceQuery(
    var segment: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCarbonReportVariance): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCarbonReportVariance>): MutableList<LenientCarbonReportVariance> {
        val matched = filter(items) { candidate: LenientCarbonReportVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCarbonReportVariances(store: LenientCarbonReportVarianceStore, query: LenientCarbonReportVarianceQuery): MutableList<LenientCarbonReportVariance> {
    return query.apply(store.list())
}
