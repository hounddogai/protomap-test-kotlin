package com.acme.transport.fleetops.defect.summary

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect summaries by code. */
interface DefectSummaryStore {
    fun find(code: String): DefectSummary?
    fun save(item: DefectSummary)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectSummary>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectSummaryStore(
    var items: MutableMap<String, DefectSummary>,
    var order: MutableList<String>,
) : DefectSummaryStore {
    override fun find(code: String): DefectSummary? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectSummary) {
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

    override fun list(): MutableList<DefectSummary> {
        val out = mutableListOf<DefectSummary>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectSummaryStore(): DefectSummaryStore {
    return MemoryDefectSummaryStore(mutableMapOf<String, DefectSummary>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectSummaryStore(
    var inner: DefectSummaryStore,
    var cache: TtlCache<DefectSummary>,
    var hits: Int,
    var misses: Int,
) : DefectSummaryStore {
    override fun find(code: String): DefectSummary? {
        val hit = (cache.get(code) as DefectSummary?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectSummary) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectSummary> = inner.list()
}

fun newCachedDefectSummaryStore(inner: DefectSummaryStore, ttl: Int, clock: Clock): DefectSummaryStore {
    return CachedDefectSummaryStore(
        inner = inner,
        cache = newTtlCache<DefectSummary>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect summaries. Empty fields match all. */
data class DefectSummaryQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectSummary): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<DefectSummary>): MutableList<DefectSummary> {
        val matched = filter(items) { candidate: DefectSummary -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectSummaries(store: DefectSummaryStore, query: DefectSummaryQuery): MutableList<DefectSummary> {
    return query.apply(store.list())
}
