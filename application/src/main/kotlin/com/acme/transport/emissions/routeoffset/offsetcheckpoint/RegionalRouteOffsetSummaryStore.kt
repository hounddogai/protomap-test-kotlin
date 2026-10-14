package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional route offset summaries by code. */
interface RegionalRouteOffsetSummaryStore {
    fun find(code: String): RegionalRouteOffsetSummary?
    fun save(item: RegionalRouteOffsetSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalRouteOffsetSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalRouteOffsetSummaryStore(
    var items: MutableMap<String, RegionalRouteOffsetSummary>,
    var order: MutableList<String>,
) : RegionalRouteOffsetSummaryStore {
    override fun find(code: String): RegionalRouteOffsetSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalRouteOffsetSummary) {
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

    override fun list(): MutableList<RegionalRouteOffsetSummary> {
        val out = mutableListOf<RegionalRouteOffsetSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalRouteOffsetSummaryStore(): RegionalRouteOffsetSummaryStore {
    return MemoryRegionalRouteOffsetSummaryStore(
        mutableMapOf<String, RegionalRouteOffsetSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalRouteOffsetSummaryStore(
    var inner: RegionalRouteOffsetSummaryStore,
    var cache: TtlCache<RegionalRouteOffsetSummary>,
    var hits: Int,
    var misses: Int,
) : RegionalRouteOffsetSummaryStore {
    override fun find(code: String): RegionalRouteOffsetSummary? {
        val hit = (cache.get(code) as RegionalRouteOffsetSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalRouteOffsetSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalRouteOffsetSummary> = inner.list()
}

fun newCachedRegionalRouteOffsetSummaryStore(inner: RegionalRouteOffsetSummaryStore, ttl: Int, clock: Clock): RegionalRouteOffsetSummaryStore {
    return CachedRegionalRouteOffsetSummaryStore(
        inner = inner,
        cache = newTtlCache<RegionalRouteOffsetSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional route offset summaries. Empty fields match all. */
data class RegionalRouteOffsetSummaryQuery(
    var source: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalRouteOffsetSummary): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalRouteOffsetSummary>): MutableList<RegionalRouteOffsetSummary> {
        val matched = filter(items) { candidate: RegionalRouteOffsetSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalRouteOffsetSummaries(store: RegionalRouteOffsetSummaryStore, query: RegionalRouteOffsetSummaryQuery): MutableList<RegionalRouteOffsetSummary> {
    return query.apply(store.list())
}
