package com.acme.transport.fleetops.inspection.segment

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling inspection summaries by code. */
interface RollingInspectionSummaryStore {
    fun find(code: String): RollingInspectionSummary?
    fun save(item: RollingInspectionSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingInspectionSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingInspectionSummaryStore(
    var items: MutableMap<String, RollingInspectionSummary>,
    var order: MutableList<String>,
) : RollingInspectionSummaryStore {
    override fun find(code: String): RollingInspectionSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingInspectionSummary) {
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

    override fun list(): MutableList<RollingInspectionSummary> {
        val out = mutableListOf<RollingInspectionSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingInspectionSummaryStore(): RollingInspectionSummaryStore {
    return MemoryRollingInspectionSummaryStore(
        mutableMapOf<String, RollingInspectionSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingInspectionSummaryStore(
    var inner: RollingInspectionSummaryStore,
    var cache: TtlCache<RollingInspectionSummary>,
    var hits: Int,
    var misses: Int,
) : RollingInspectionSummaryStore {
    override fun find(code: String): RollingInspectionSummary? {
        val hit = (cache.get(code) as RollingInspectionSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingInspectionSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingInspectionSummary> = inner.list()
}

fun newCachedRollingInspectionSummaryStore(inner: RollingInspectionSummaryStore, ttl: Int, clock: Clock): RollingInspectionSummaryStore {
    return CachedRollingInspectionSummaryStore(
        inner = inner,
        cache = newTtlCache<RollingInspectionSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling inspection summaries. Empty fields match all. */
data class RollingInspectionSummaryQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingInspectionSummary): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingInspectionSummary>): MutableList<RollingInspectionSummary> {
        val matched = filter(items) { candidate: RollingInspectionSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingInspectionSummaries(store: RollingInspectionSummaryStore, query: RollingInspectionSummaryQuery): MutableList<RollingInspectionSummary> {
    return query.apply(store.list())
}
