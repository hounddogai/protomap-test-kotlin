package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered carbon report adjustments by code. */
interface TieredCarbonReportAdjustmentStore {
    fun find(code: String): TieredCarbonReportAdjustment?
    fun save(item: TieredCarbonReportAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCarbonReportAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCarbonReportAdjustmentStore(
    var items: MutableMap<String, TieredCarbonReportAdjustment>,
    var order: MutableList<String>,
) : TieredCarbonReportAdjustmentStore {
    override fun find(code: String): TieredCarbonReportAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCarbonReportAdjustment) {
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

    override fun list(): MutableList<TieredCarbonReportAdjustment> {
        val out = mutableListOf<TieredCarbonReportAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCarbonReportAdjustmentStore(): TieredCarbonReportAdjustmentStore {
    return MemoryTieredCarbonReportAdjustmentStore(
        mutableMapOf<String, TieredCarbonReportAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCarbonReportAdjustmentStore(
    var inner: TieredCarbonReportAdjustmentStore,
    var cache: TtlCache<TieredCarbonReportAdjustment>,
    var hits: Int,
    var misses: Int,
) : TieredCarbonReportAdjustmentStore {
    override fun find(code: String): TieredCarbonReportAdjustment? {
        val hit = (cache.get(code) as TieredCarbonReportAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCarbonReportAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCarbonReportAdjustment> = inner.list()
}

fun newCachedTieredCarbonReportAdjustmentStore(inner: TieredCarbonReportAdjustmentStore, ttl: Int, clock: Clock): TieredCarbonReportAdjustmentStore {
    return CachedTieredCarbonReportAdjustmentStore(
        inner = inner,
        cache = newTtlCache<TieredCarbonReportAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered carbon report adjustments. Empty fields match all. */
data class TieredCarbonReportAdjustmentQuery(
    var category: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCarbonReportAdjustment): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCarbonReportAdjustment>): MutableList<TieredCarbonReportAdjustment> {
        val matched = filter(items) { candidate: TieredCarbonReportAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCarbonReportAdjustments(store: TieredCarbonReportAdjustmentStore, query: TieredCarbonReportAdjustmentQuery): MutableList<TieredCarbonReportAdjustment> {
    return query.apply(store.list())
}
