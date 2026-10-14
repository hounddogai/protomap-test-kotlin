package com.acme.transport.fleetops.inspection.inspectionsummary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped inspection summaries by code. */
interface CappedInspectionSummaryStore {
    fun find(code: String): CappedInspectionSummary?
    fun save(item: CappedInspectionSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedInspectionSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedInspectionSummaryStore(
    var items: MutableMap<String, CappedInspectionSummary>,
    var order: MutableList<String>,
) : CappedInspectionSummaryStore {
    override fun find(code: String): CappedInspectionSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedInspectionSummary) {
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

    override fun list(): MutableList<CappedInspectionSummary> {
        val out = mutableListOf<CappedInspectionSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedInspectionSummaryStore(): CappedInspectionSummaryStore {
    return MemoryCappedInspectionSummaryStore(
        mutableMapOf<String, CappedInspectionSummary>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedInspectionSummaryStore(
    var inner: CappedInspectionSummaryStore,
    var cache: TtlCache<CappedInspectionSummary>,
    var hits: Int,
    var misses: Int,
) : CappedInspectionSummaryStore {
    override fun find(code: String): CappedInspectionSummary? {
        val hit = (cache.get(code) as CappedInspectionSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedInspectionSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedInspectionSummary> = inner.list()
}

fun newCachedCappedInspectionSummaryStore(inner: CappedInspectionSummaryStore, ttl: Int, clock: Clock): CappedInspectionSummaryStore {
    return CachedCappedInspectionSummaryStore(
        inner = inner,
        cache = newTtlCache<CappedInspectionSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped inspection summaries. Empty fields match all. */
data class CappedInspectionSummaryQuery(
    var unit: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedInspectionSummary): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedInspectionSummary>): MutableList<CappedInspectionSummary> {
        val matched = filter(items) { candidate: CappedInspectionSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedInspectionSummaries(store: CappedInspectionSummaryStore, query: CappedInspectionSummaryQuery): MutableList<CappedInspectionSummary> {
    return query.apply(store.list())
}
