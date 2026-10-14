package com.acme.transport.fleetops.utilization.summary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization summaries by code. */
interface UtilizationSummaryStore {
    fun find(code: String): UtilizationSummary?
    fun save(item: UtilizationSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationSummaryStore(
    var items: MutableMap<String, UtilizationSummary>,
    var order: MutableList<String>,
) : UtilizationSummaryStore {
    override fun find(code: String): UtilizationSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationSummary) {
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

    override fun list(): MutableList<UtilizationSummary> {
        val out = mutableListOf<UtilizationSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationSummaryStore(): UtilizationSummaryStore {
    return MemoryUtilizationSummaryStore(mutableMapOf<String, UtilizationSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationSummaryStore(
    var inner: UtilizationSummaryStore,
    var cache: TtlCache<UtilizationSummary>,
    var hits: Int,
    var misses: Int,
) : UtilizationSummaryStore {
    override fun find(code: String): UtilizationSummary? {
        val hit = (cache.get(code) as UtilizationSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationSummary> = inner.list()
}

fun newCachedUtilizationSummaryStore(inner: UtilizationSummaryStore, ttl: Int, clock: Clock): UtilizationSummaryStore {
    return CachedUtilizationSummaryStore(
        inner = inner,
        cache = newTtlCache<UtilizationSummary>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization summaries. Empty fields match all. */
data class UtilizationSummaryQuery(
    var label: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationSummary): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<UtilizationSummary>): MutableList<UtilizationSummary> {
        val matched = filter(items) { candidate: UtilizationSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationSummaries(store: UtilizationSummaryStore, query: UtilizationSummaryQuery): MutableList<UtilizationSummary> {
    return query.apply(store.list())
}
