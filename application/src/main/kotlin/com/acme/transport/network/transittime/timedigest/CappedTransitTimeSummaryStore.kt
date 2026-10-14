package com.acme.transport.network.transittime.timedigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps capped transit time summaries by code. */
interface CappedTransitTimeSummaryStore {
    fun find(code: String): CappedTransitTimeSummary?
    fun save(item: CappedTransitTimeSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedTransitTimeSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedTransitTimeSummaryStore(
    var items: MutableMap<String, CappedTransitTimeSummary>,
    var order: MutableList<String>,
) : CappedTransitTimeSummaryStore {
    override fun find(code: String): CappedTransitTimeSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedTransitTimeSummary) {
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

    override fun list(): MutableList<CappedTransitTimeSummary> {
        val out = mutableListOf<CappedTransitTimeSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedTransitTimeSummaryStore(): CappedTransitTimeSummaryStore {
    return MemoryCappedTransitTimeSummaryStore(
        mutableMapOf<String, CappedTransitTimeSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedTransitTimeSummaryStore(
    var inner: CappedTransitTimeSummaryStore,
    var cache: TtlCache<CappedTransitTimeSummary>,
    var hits: Int,
    var misses: Int,
) : CappedTransitTimeSummaryStore {
    override fun find(code: String): CappedTransitTimeSummary? {
        val hit = (cache.get(code) as CappedTransitTimeSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedTransitTimeSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedTransitTimeSummary> = inner.list()
}

fun newCachedCappedTransitTimeSummaryStore(inner: CappedTransitTimeSummaryStore, ttl: Int, clock: Clock): CappedTransitTimeSummaryStore {
    return CachedCappedTransitTimeSummaryStore(
        inner = inner,
        cache = newTtlCache<CappedTransitTimeSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped transit time summaries. Empty fields match all. */
data class CappedTransitTimeSummaryQuery(
    var lane: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedTransitTimeSummary): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<CappedTransitTimeSummary>): MutableList<CappedTransitTimeSummary> {
        val matched = filter(items) { candidate: CappedTransitTimeSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedTransitTimeSummaries(store: CappedTransitTimeSummaryStore, query: CappedTransitTimeSummaryQuery): MutableList<CappedTransitTimeSummary> {
    return query.apply(store.list())
}
