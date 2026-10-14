package com.acme.transport.terminal.traileryard.yardsummary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps seasonal trailer yard summaries by code. */
interface SeasonalTrailerYardSummaryStore {
    fun find(code: String): SeasonalTrailerYardSummary?
    fun save(item: SeasonalTrailerYardSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTrailerYardSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTrailerYardSummaryStore(
    var items: MutableMap<String, SeasonalTrailerYardSummary>,
    var order: MutableList<String>,
) : SeasonalTrailerYardSummaryStore {
    override fun find(code: String): SeasonalTrailerYardSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTrailerYardSummary) {
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

    override fun list(): MutableList<SeasonalTrailerYardSummary> {
        val out = mutableListOf<SeasonalTrailerYardSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTrailerYardSummaryStore(): SeasonalTrailerYardSummaryStore {
    return MemorySeasonalTrailerYardSummaryStore(
        mutableMapOf<String, SeasonalTrailerYardSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTrailerYardSummaryStore(
    var inner: SeasonalTrailerYardSummaryStore,
    var cache: TtlCache<SeasonalTrailerYardSummary>,
    var hits: Int,
    var misses: Int,
) : SeasonalTrailerYardSummaryStore {
    override fun find(code: String): SeasonalTrailerYardSummary? {
        val hit = (cache.get(code) as SeasonalTrailerYardSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTrailerYardSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTrailerYardSummary> = inner.list()
}

fun newCachedSeasonalTrailerYardSummaryStore(inner: SeasonalTrailerYardSummaryStore, ttl: Int, clock: Clock): SeasonalTrailerYardSummaryStore {
    return CachedSeasonalTrailerYardSummaryStore(
        inner = inner,
        cache = newTtlCache<SeasonalTrailerYardSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal trailer yard summaries. Empty fields match all. */
data class SeasonalTrailerYardSummaryQuery(
    var lane: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTrailerYardSummary): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTrailerYardSummary>): MutableList<SeasonalTrailerYardSummary> {
        val matched = filter(items) { candidate: SeasonalTrailerYardSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTrailerYardSummaries(store: SeasonalTrailerYardSummaryStore, query: SeasonalTrailerYardSummaryQuery): MutableList<SeasonalTrailerYardSummary> {
    return query.apply(store.list())
}
