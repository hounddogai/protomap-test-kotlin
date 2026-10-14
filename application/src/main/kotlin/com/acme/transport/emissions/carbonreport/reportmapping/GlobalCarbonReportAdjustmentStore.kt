package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global carbon report adjustments by code. */
interface GlobalCarbonReportAdjustmentStore {
    fun find(code: String): GlobalCarbonReportAdjustment?
    fun save(item: GlobalCarbonReportAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalCarbonReportAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalCarbonReportAdjustmentStore(
    var items: MutableMap<String, GlobalCarbonReportAdjustment>,
    var order: MutableList<String>,
) : GlobalCarbonReportAdjustmentStore {
    override fun find(code: String): GlobalCarbonReportAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalCarbonReportAdjustment) {
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

    override fun list(): MutableList<GlobalCarbonReportAdjustment> {
        val out = mutableListOf<GlobalCarbonReportAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalCarbonReportAdjustmentStore(): GlobalCarbonReportAdjustmentStore {
    return MemoryGlobalCarbonReportAdjustmentStore(
        mutableMapOf<String, GlobalCarbonReportAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalCarbonReportAdjustmentStore(
    var inner: GlobalCarbonReportAdjustmentStore,
    var cache: TtlCache<GlobalCarbonReportAdjustment>,
    var hits: Int,
    var misses: Int,
) : GlobalCarbonReportAdjustmentStore {
    override fun find(code: String): GlobalCarbonReportAdjustment? {
        val hit = (cache.get(code) as GlobalCarbonReportAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalCarbonReportAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalCarbonReportAdjustment> = inner.list()
}

fun newCachedGlobalCarbonReportAdjustmentStore(inner: GlobalCarbonReportAdjustmentStore, ttl: Int, clock: Clock): GlobalCarbonReportAdjustmentStore {
    return CachedGlobalCarbonReportAdjustmentStore(
        inner = inner,
        cache = newTtlCache<GlobalCarbonReportAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global carbon report adjustments. Empty fields match all. */
data class GlobalCarbonReportAdjustmentQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalCarbonReportAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<GlobalCarbonReportAdjustment>): MutableList<GlobalCarbonReportAdjustment> {
        val matched = filter(items) { candidate: GlobalCarbonReportAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalCarbonReportAdjustments(store: GlobalCarbonReportAdjustmentStore, query: GlobalCarbonReportAdjustmentQuery): MutableList<GlobalCarbonReportAdjustment> {
    return query.apply(store.list())
}
