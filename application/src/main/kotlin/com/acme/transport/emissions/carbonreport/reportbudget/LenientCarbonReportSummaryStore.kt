package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient carbon report summaries by code. */
interface LenientCarbonReportSummaryStore {
    fun find(code: String): LenientCarbonReportSummary?
    fun save(item: LenientCarbonReportSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCarbonReportSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCarbonReportSummaryStore(
    var items: MutableMap<String, LenientCarbonReportSummary>,
    var order: MutableList<String>,
) : LenientCarbonReportSummaryStore {
    override fun find(code: String): LenientCarbonReportSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCarbonReportSummary) {
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

    override fun list(): MutableList<LenientCarbonReportSummary> {
        val out = mutableListOf<LenientCarbonReportSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCarbonReportSummaryStore(): LenientCarbonReportSummaryStore {
    return MemoryLenientCarbonReportSummaryStore(
        mutableMapOf<String, LenientCarbonReportSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCarbonReportSummaryStore(
    var inner: LenientCarbonReportSummaryStore,
    var cache: TtlCache<LenientCarbonReportSummary>,
    var hits: Int,
    var misses: Int,
) : LenientCarbonReportSummaryStore {
    override fun find(code: String): LenientCarbonReportSummary? {
        val hit = (cache.get(code) as LenientCarbonReportSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCarbonReportSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCarbonReportSummary> = inner.list()
}

fun newCachedLenientCarbonReportSummaryStore(inner: LenientCarbonReportSummaryStore, ttl: Int, clock: Clock): LenientCarbonReportSummaryStore {
    return CachedLenientCarbonReportSummaryStore(
        inner = inner,
        cache = newTtlCache<LenientCarbonReportSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient carbon report summaries. Empty fields match all. */
data class LenientCarbonReportSummaryQuery(
    var region: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCarbonReportSummary): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<LenientCarbonReportSummary>): MutableList<LenientCarbonReportSummary> {
        val matched = filter(items) { candidate: LenientCarbonReportSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCarbonReportSummaries(store: LenientCarbonReportSummaryStore, query: LenientCarbonReportSummaryQuery): MutableList<LenientCarbonReportSummary> {
    return query.apply(store.list())
}
