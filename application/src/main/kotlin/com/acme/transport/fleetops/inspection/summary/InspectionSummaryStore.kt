package com.acme.transport.fleetops.inspection.summary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection summaries by code. */
interface InspectionSummaryStore {
    fun find(code: String): InspectionSummary?
    fun save(item: InspectionSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionSummaryStore(
    var items: MutableMap<String, InspectionSummary>,
    var order: MutableList<String>,
) : InspectionSummaryStore {
    override fun find(code: String): InspectionSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionSummary) {
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

    override fun list(): MutableList<InspectionSummary> {
        val out = mutableListOf<InspectionSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionSummaryStore(): InspectionSummaryStore {
    return MemoryInspectionSummaryStore(mutableMapOf<String, InspectionSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionSummaryStore(
    var inner: InspectionSummaryStore,
    var cache: TtlCache<InspectionSummary>,
    var hits: Int,
    var misses: Int,
) : InspectionSummaryStore {
    override fun find(code: String): InspectionSummary? {
        val hit = (cache.get(code) as InspectionSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionSummary> = inner.list()
}

fun newCachedInspectionSummaryStore(inner: InspectionSummaryStore, ttl: Int, clock: Clock): InspectionSummaryStore {
    return CachedInspectionSummaryStore(
        inner = inner,
        cache = newTtlCache<InspectionSummary>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection summaries. Empty fields match all. */
data class InspectionSummaryQuery(
    var channel: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionSummary): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionSummary>): MutableList<InspectionSummary> {
        val matched = filter(items) { candidate: InspectionSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionSummaries(store: InspectionSummaryStore, query: InspectionSummaryQuery): MutableList<InspectionSummary> {
    return query.apply(store.list())
}
