package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary carbon report adjustments by code. */
interface PrimaryCarbonReportAdjustmentStore {
    fun find(code: String): PrimaryCarbonReportAdjustment?
    fun save(item: PrimaryCarbonReportAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryCarbonReportAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryCarbonReportAdjustmentStore(
    var items: MutableMap<String, PrimaryCarbonReportAdjustment>,
    var order: MutableList<String>,
) : PrimaryCarbonReportAdjustmentStore {
    override fun find(code: String): PrimaryCarbonReportAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryCarbonReportAdjustment) {
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

    override fun list(): MutableList<PrimaryCarbonReportAdjustment> {
        val out = mutableListOf<PrimaryCarbonReportAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryCarbonReportAdjustmentStore(): PrimaryCarbonReportAdjustmentStore {
    return MemoryPrimaryCarbonReportAdjustmentStore(
        mutableMapOf<String, PrimaryCarbonReportAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryCarbonReportAdjustmentStore(
    var inner: PrimaryCarbonReportAdjustmentStore,
    var cache: TtlCache<PrimaryCarbonReportAdjustment>,
    var hits: Int,
    var misses: Int,
) : PrimaryCarbonReportAdjustmentStore {
    override fun find(code: String): PrimaryCarbonReportAdjustment? {
        val hit = (cache.get(code) as PrimaryCarbonReportAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryCarbonReportAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryCarbonReportAdjustment> = inner.list()
}

fun newCachedPrimaryCarbonReportAdjustmentStore(inner: PrimaryCarbonReportAdjustmentStore, ttl: Int, clock: Clock): PrimaryCarbonReportAdjustmentStore {
    return CachedPrimaryCarbonReportAdjustmentStore(
        inner = inner,
        cache = newTtlCache<PrimaryCarbonReportAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary carbon report adjustments. Empty fields match all. */
data class PrimaryCarbonReportAdjustmentQuery(
    var category: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryCarbonReportAdjustment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryCarbonReportAdjustment>): MutableList<PrimaryCarbonReportAdjustment> {
        val matched = filter(items) { candidate: PrimaryCarbonReportAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryCarbonReportAdjustments(store: PrimaryCarbonReportAdjustmentStore, query: PrimaryCarbonReportAdjustmentQuery): MutableList<PrimaryCarbonReportAdjustment> {
    return query.apply(store.list())
}
