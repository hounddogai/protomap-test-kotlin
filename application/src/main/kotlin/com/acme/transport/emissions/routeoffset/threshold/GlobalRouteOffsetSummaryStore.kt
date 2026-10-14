package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global route offset summaries by code. */
interface GlobalRouteOffsetSummaryStore {
    fun find(code: String): GlobalRouteOffsetSummary?
    fun save(item: GlobalRouteOffsetSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalRouteOffsetSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalRouteOffsetSummaryStore(
    var items: MutableMap<String, GlobalRouteOffsetSummary>,
    var order: MutableList<String>,
) : GlobalRouteOffsetSummaryStore {
    override fun find(code: String): GlobalRouteOffsetSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalRouteOffsetSummary) {
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

    override fun list(): MutableList<GlobalRouteOffsetSummary> {
        val out = mutableListOf<GlobalRouteOffsetSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalRouteOffsetSummaryStore(): GlobalRouteOffsetSummaryStore {
    return MemoryGlobalRouteOffsetSummaryStore(
        mutableMapOf<String, GlobalRouteOffsetSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalRouteOffsetSummaryStore(
    var inner: GlobalRouteOffsetSummaryStore,
    var cache: TtlCache<GlobalRouteOffsetSummary>,
    var hits: Int,
    var misses: Int,
) : GlobalRouteOffsetSummaryStore {
    override fun find(code: String): GlobalRouteOffsetSummary? {
        val hit = (cache.get(code) as GlobalRouteOffsetSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalRouteOffsetSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalRouteOffsetSummary> = inner.list()
}

fun newCachedGlobalRouteOffsetSummaryStore(inner: GlobalRouteOffsetSummaryStore, ttl: Int, clock: Clock): GlobalRouteOffsetSummaryStore {
    return CachedGlobalRouteOffsetSummaryStore(
        inner = inner,
        cache = newTtlCache<GlobalRouteOffsetSummary>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global route offset summaries. Empty fields match all. */
data class GlobalRouteOffsetSummaryQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalRouteOffsetSummary): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalRouteOffsetSummary>): MutableList<GlobalRouteOffsetSummary> {
        val matched = filter(items) { candidate: GlobalRouteOffsetSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalRouteOffsetSummaries(store: GlobalRouteOffsetSummaryStore, query: GlobalRouteOffsetSummaryQuery): MutableList<GlobalRouteOffsetSummary> {
    return query.apply(store.list())
}
