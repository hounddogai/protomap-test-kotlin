package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending utilization summaries by code. */
interface PendingUtilizationSummaryStore {
    fun find(code: String): PendingUtilizationSummary?
    fun save(item: PendingUtilizationSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingUtilizationSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingUtilizationSummaryStore(
    var items: MutableMap<String, PendingUtilizationSummary>,
    var order: MutableList<String>,
) : PendingUtilizationSummaryStore {
    override fun find(code: String): PendingUtilizationSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingUtilizationSummary) {
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

    override fun list(): MutableList<PendingUtilizationSummary> {
        val out = mutableListOf<PendingUtilizationSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingUtilizationSummaryStore(): PendingUtilizationSummaryStore {
    return MemoryPendingUtilizationSummaryStore(
        mutableMapOf<String, PendingUtilizationSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingUtilizationSummaryStore(
    var inner: PendingUtilizationSummaryStore,
    var cache: TtlCache<PendingUtilizationSummary>,
    var hits: Int,
    var misses: Int,
) : PendingUtilizationSummaryStore {
    override fun find(code: String): PendingUtilizationSummary? {
        val hit = (cache.get(code) as PendingUtilizationSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingUtilizationSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingUtilizationSummary> = inner.list()
}

fun newCachedPendingUtilizationSummaryStore(inner: PendingUtilizationSummaryStore, ttl: Int, clock: Clock): PendingUtilizationSummaryStore {
    return CachedPendingUtilizationSummaryStore(
        inner = inner,
        cache = newTtlCache<PendingUtilizationSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending utilization summaries. Empty fields match all. */
data class PendingUtilizationSummaryQuery(
    var region: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingUtilizationSummary): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingUtilizationSummary>): MutableList<PendingUtilizationSummary> {
        val matched = filter(items) { candidate: PendingUtilizationSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingUtilizationSummaries(store: PendingUtilizationSummaryStore, query: PendingUtilizationSummaryQuery): MutableList<PendingUtilizationSummary> {
    return query.apply(store.list())
}
