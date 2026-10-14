package com.acme.transport.linehaul.trailer.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal trailer summaries by code. */
interface SeasonalTrailerSummaryStore {
    fun find(code: String): SeasonalTrailerSummary?
    fun save(item: SeasonalTrailerSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalTrailerSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalTrailerSummaryStore(
    var items: MutableMap<String, SeasonalTrailerSummary>,
    var order: MutableList<String>,
) : SeasonalTrailerSummaryStore {
    override fun find(code: String): SeasonalTrailerSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalTrailerSummary) {
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

    override fun list(): MutableList<SeasonalTrailerSummary> {
        val out = mutableListOf<SeasonalTrailerSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalTrailerSummaryStore(): SeasonalTrailerSummaryStore {
    return MemorySeasonalTrailerSummaryStore(
        mutableMapOf<String, SeasonalTrailerSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalTrailerSummaryStore(
    var inner: SeasonalTrailerSummaryStore,
    var cache: TtlCache<SeasonalTrailerSummary>,
    var hits: Int,
    var misses: Int,
) : SeasonalTrailerSummaryStore {
    override fun find(code: String): SeasonalTrailerSummary? {
        val hit = (cache.get(code) as SeasonalTrailerSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalTrailerSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalTrailerSummary> = inner.list()
}

fun newCachedSeasonalTrailerSummaryStore(inner: SeasonalTrailerSummaryStore, ttl: Int, clock: Clock): SeasonalTrailerSummaryStore {
    return CachedSeasonalTrailerSummaryStore(
        inner = inner,
        cache = newTtlCache<SeasonalTrailerSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal trailer summaries. Empty fields match all. */
data class SeasonalTrailerSummaryQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalTrailerSummary): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalTrailerSummary>): MutableList<SeasonalTrailerSummary> {
        val matched = filter(items) { candidate: SeasonalTrailerSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalTrailerSummaries(store: SeasonalTrailerSummaryStore, query: SeasonalTrailerSummaryQuery): MutableList<SeasonalTrailerSummary> {
    return query.apply(store.list())
}
