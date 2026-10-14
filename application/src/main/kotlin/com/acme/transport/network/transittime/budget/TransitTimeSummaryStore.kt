package com.acme.transport.network.transittime.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time summaries by code. */
interface TransitTimeSummaryStore {
    fun find(code: String): TransitTimeSummary?
    fun save(item: TransitTimeSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeSummaryStore(
    var items: MutableMap<String, TransitTimeSummary>,
    var order: MutableList<String>,
) : TransitTimeSummaryStore {
    override fun find(code: String): TransitTimeSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeSummary) {
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

    override fun list(): MutableList<TransitTimeSummary> {
        val out = mutableListOf<TransitTimeSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeSummaryStore(): TransitTimeSummaryStore {
    return MemoryTransitTimeSummaryStore(mutableMapOf<String, TransitTimeSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeSummaryStore(
    var inner: TransitTimeSummaryStore,
    var cache: TtlCache<TransitTimeSummary>,
    var hits: Int,
    var misses: Int,
) : TransitTimeSummaryStore {
    override fun find(code: String): TransitTimeSummary? {
        val hit = (cache.get(code) as TransitTimeSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeSummary> = inner.list()
}

fun newCachedTransitTimeSummaryStore(inner: TransitTimeSummaryStore, ttl: Int, clock: Clock): TransitTimeSummaryStore {
    return CachedTransitTimeSummaryStore(
        inner = inner,
        cache = newTtlCache<TransitTimeSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time summaries. Empty fields match all. */
data class TransitTimeSummaryQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeSummary): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<TransitTimeSummary>): MutableList<TransitTimeSummary> {
        val matched = filter(items) { candidate: TransitTimeSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeSummaries(store: TransitTimeSummaryStore, query: TransitTimeSummaryQuery): MutableList<TransitTimeSummary> {
    return query.apply(store.list())
}
