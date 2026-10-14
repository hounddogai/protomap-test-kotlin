package com.acme.transport.linehaul.trailer.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer summaries by code. */
interface TrailerSummaryStore {
    fun find(code: String): TrailerSummary?
    fun save(item: TrailerSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerSummaryStore(
    var items: MutableMap<String, TrailerSummary>,
    var order: MutableList<String>,
) : TrailerSummaryStore {
    override fun find(code: String): TrailerSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerSummary) {
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

    override fun list(): MutableList<TrailerSummary> {
        val out = mutableListOf<TrailerSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerSummaryStore(): TrailerSummaryStore {
    return MemoryTrailerSummaryStore(mutableMapOf<String, TrailerSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerSummaryStore(
    var inner: TrailerSummaryStore,
    var cache: TtlCache<TrailerSummary>,
    var hits: Int,
    var misses: Int,
) : TrailerSummaryStore {
    override fun find(code: String): TrailerSummary? {
        val hit = (cache.get(code) as TrailerSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerSummary> = inner.list()
}

fun newCachedTrailerSummaryStore(inner: TrailerSummaryStore, ttl: Int, clock: Clock): TrailerSummaryStore {
    return CachedTrailerSummaryStore(
        inner = inner,
        cache = newTtlCache<TrailerSummary>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer summaries. Empty fields match all. */
data class TrailerSummaryQuery(
    var lane: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerSummary): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<TrailerSummary>): MutableList<TrailerSummary> {
        val matched = filter(items) { candidate: TrailerSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerSummaries(store: TrailerSummaryStore, query: TrailerSummaryQuery): MutableList<TrailerSummary> {
    return query.apply(store.list())
}
