package com.acme.transport.terminal.traileryard.yardreservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps regional trailer yard summaries by code. */
interface RegionalTrailerYardSummaryStore {
    fun find(code: String): RegionalTrailerYardSummary?
    fun save(item: RegionalTrailerYardSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalTrailerYardSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalTrailerYardSummaryStore(
    var items: MutableMap<String, RegionalTrailerYardSummary>,
    var order: MutableList<String>,
) : RegionalTrailerYardSummaryStore {
    override fun find(code: String): RegionalTrailerYardSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalTrailerYardSummary) {
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

    override fun list(): MutableList<RegionalTrailerYardSummary> {
        val out = mutableListOf<RegionalTrailerYardSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalTrailerYardSummaryStore(): RegionalTrailerYardSummaryStore {
    return MemoryRegionalTrailerYardSummaryStore(
        mutableMapOf<String, RegionalTrailerYardSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalTrailerYardSummaryStore(
    var inner: RegionalTrailerYardSummaryStore,
    var cache: TtlCache<RegionalTrailerYardSummary>,
    var hits: Int,
    var misses: Int,
) : RegionalTrailerYardSummaryStore {
    override fun find(code: String): RegionalTrailerYardSummary? {
        val hit = (cache.get(code) as RegionalTrailerYardSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalTrailerYardSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalTrailerYardSummary> = inner.list()
}

fun newCachedRegionalTrailerYardSummaryStore(inner: RegionalTrailerYardSummaryStore, ttl: Int, clock: Clock): RegionalTrailerYardSummaryStore {
    return CachedRegionalTrailerYardSummaryStore(
        inner = inner,
        cache = newTtlCache<RegionalTrailerYardSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional trailer yard summaries. Empty fields match all. */
data class RegionalTrailerYardSummaryQuery(
    var category: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalTrailerYardSummary): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<RegionalTrailerYardSummary>): MutableList<RegionalTrailerYardSummary> {
        val matched = filter(items) { candidate: RegionalTrailerYardSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalTrailerYardSummaries(store: RegionalTrailerYardSummaryStore, query: RegionalTrailerYardSummaryQuery): MutableList<RegionalTrailerYardSummary> {
    return query.apply(store.list())
}
