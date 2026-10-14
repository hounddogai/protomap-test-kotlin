package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled route offset summaries by code. */
interface ScheduledRouteOffsetSummaryStore {
    fun find(code: String): ScheduledRouteOffsetSummary?
    fun save(item: ScheduledRouteOffsetSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRouteOffsetSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRouteOffsetSummaryStore(
    var items: MutableMap<String, ScheduledRouteOffsetSummary>,
    var order: MutableList<String>,
) : ScheduledRouteOffsetSummaryStore {
    override fun find(code: String): ScheduledRouteOffsetSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRouteOffsetSummary) {
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

    override fun list(): MutableList<ScheduledRouteOffsetSummary> {
        val out = mutableListOf<ScheduledRouteOffsetSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRouteOffsetSummaryStore(): ScheduledRouteOffsetSummaryStore {
    return MemoryScheduledRouteOffsetSummaryStore(
        mutableMapOf<String, ScheduledRouteOffsetSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRouteOffsetSummaryStore(
    var inner: ScheduledRouteOffsetSummaryStore,
    var cache: TtlCache<ScheduledRouteOffsetSummary>,
    var hits: Int,
    var misses: Int,
) : ScheduledRouteOffsetSummaryStore {
    override fun find(code: String): ScheduledRouteOffsetSummary? {
        val hit = (cache.get(code) as ScheduledRouteOffsetSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRouteOffsetSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRouteOffsetSummary> = inner.list()
}

fun newCachedScheduledRouteOffsetSummaryStore(inner: ScheduledRouteOffsetSummaryStore, ttl: Int, clock: Clock): ScheduledRouteOffsetSummaryStore {
    return CachedScheduledRouteOffsetSummaryStore(
        inner = inner,
        cache = newTtlCache<ScheduledRouteOffsetSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled route offset summaries. Empty fields match all. */
data class ScheduledRouteOffsetSummaryQuery(
    var unit: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRouteOffsetSummary): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRouteOffsetSummary>): MutableList<ScheduledRouteOffsetSummary> {
        val matched = filter(items) { candidate: ScheduledRouteOffsetSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRouteOffsetSummaries(store: ScheduledRouteOffsetSummaryStore, query: ScheduledRouteOffsetSummaryQuery): MutableList<ScheduledRouteOffsetSummary> {
    return query.apply(store.list())
}
