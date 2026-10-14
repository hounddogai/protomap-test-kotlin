package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps route offset summaries by code. */
interface RouteOffsetSummaryStore {
    fun find(code: String): RouteOffsetSummary?
    fun save(item: RouteOffsetSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<RouteOffsetSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRouteOffsetSummaryStore(
    var items: MutableMap<String, RouteOffsetSummary>,
    var order: MutableList<String>,
) : RouteOffsetSummaryStore {
    override fun find(code: String): RouteOffsetSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RouteOffsetSummary) {
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

    override fun list(): MutableList<RouteOffsetSummary> {
        val out = mutableListOf<RouteOffsetSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRouteOffsetSummaryStore(): RouteOffsetSummaryStore {
    return MemoryRouteOffsetSummaryStore(mutableMapOf<String, RouteOffsetSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRouteOffsetSummaryStore(
    var inner: RouteOffsetSummaryStore,
    var cache: TtlCache<RouteOffsetSummary>,
    var hits: Int,
    var misses: Int,
) : RouteOffsetSummaryStore {
    override fun find(code: String): RouteOffsetSummary? {
        val hit = (cache.get(code) as RouteOffsetSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RouteOffsetSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RouteOffsetSummary> = inner.list()
}

fun newCachedRouteOffsetSummaryStore(inner: RouteOffsetSummaryStore, ttl: Int, clock: Clock): RouteOffsetSummaryStore {
    return CachedRouteOffsetSummaryStore(
        inner = inner,
        cache = newTtlCache<RouteOffsetSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects route offset summaries. Empty fields match all. */
data class RouteOffsetSummaryQuery(
    var title: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RouteOffsetSummary): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RouteOffsetSummary>): MutableList<RouteOffsetSummary> {
        val matched = filter(items) { candidate: RouteOffsetSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRouteOffsetSummaries(store: RouteOffsetSummaryStore, query: RouteOffsetSummaryQuery): MutableList<RouteOffsetSummary> {
    return query.apply(store.list())
}
