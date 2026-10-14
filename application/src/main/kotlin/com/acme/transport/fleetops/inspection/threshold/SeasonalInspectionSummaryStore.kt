package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps seasonal inspection summaries by code. */
interface SeasonalInspectionSummaryStore {
    fun find(code: String): SeasonalInspectionSummary?
    fun save(item: SeasonalInspectionSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalInspectionSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalInspectionSummaryStore(
    var items: MutableMap<String, SeasonalInspectionSummary>,
    var order: MutableList<String>,
) : SeasonalInspectionSummaryStore {
    override fun find(code: String): SeasonalInspectionSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalInspectionSummary) {
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

    override fun list(): MutableList<SeasonalInspectionSummary> {
        val out = mutableListOf<SeasonalInspectionSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalInspectionSummaryStore(): SeasonalInspectionSummaryStore {
    return MemorySeasonalInspectionSummaryStore(
        mutableMapOf<String, SeasonalInspectionSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalInspectionSummaryStore(
    var inner: SeasonalInspectionSummaryStore,
    var cache: TtlCache<SeasonalInspectionSummary>,
    var hits: Int,
    var misses: Int,
) : SeasonalInspectionSummaryStore {
    override fun find(code: String): SeasonalInspectionSummary? {
        val hit = (cache.get(code) as SeasonalInspectionSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalInspectionSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalInspectionSummary> = inner.list()
}

fun newCachedSeasonalInspectionSummaryStore(inner: SeasonalInspectionSummaryStore, ttl: Int, clock: Clock): SeasonalInspectionSummaryStore {
    return CachedSeasonalInspectionSummaryStore(
        inner = inner,
        cache = newTtlCache<SeasonalInspectionSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal inspection summaries. Empty fields match all. */
data class SeasonalInspectionSummaryQuery(
    var segment: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalInspectionSummary): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalInspectionSummary>): MutableList<SeasonalInspectionSummary> {
        val matched = filter(items) { candidate: SeasonalInspectionSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalInspectionSummaries(store: SeasonalInspectionSummaryStore, query: SeasonalInspectionSummaryQuery): MutableList<SeasonalInspectionSummary> {
    return query.apply(store.list())
}
