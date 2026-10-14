package com.acme.transport.emissions.carbonreport.recorditem

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted carbon report records by code. */
interface WeightedCarbonReportRecordStore {
    fun find(code: String): WeightedCarbonReportRecord?
    fun save(item: WeightedCarbonReportRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCarbonReportRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCarbonReportRecordStore(
    var items: MutableMap<String, WeightedCarbonReportRecord>,
    var order: MutableList<String>,
) : WeightedCarbonReportRecordStore {
    override fun find(code: String): WeightedCarbonReportRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCarbonReportRecord) {
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

    override fun list(): MutableList<WeightedCarbonReportRecord> {
        val out = mutableListOf<WeightedCarbonReportRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCarbonReportRecordStore(): WeightedCarbonReportRecordStore {
    return MemoryWeightedCarbonReportRecordStore(
        mutableMapOf<String, WeightedCarbonReportRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCarbonReportRecordStore(
    var inner: WeightedCarbonReportRecordStore,
    var cache: TtlCache<WeightedCarbonReportRecord>,
    var hits: Int,
    var misses: Int,
) : WeightedCarbonReportRecordStore {
    override fun find(code: String): WeightedCarbonReportRecord? {
        val hit = (cache.get(code) as WeightedCarbonReportRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCarbonReportRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCarbonReportRecord> = inner.list()
}

fun newCachedWeightedCarbonReportRecordStore(inner: WeightedCarbonReportRecordStore, ttl: Int, clock: Clock): WeightedCarbonReportRecordStore {
    return CachedWeightedCarbonReportRecordStore(
        inner = inner,
        cache = newTtlCache<WeightedCarbonReportRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted carbon report records. Empty fields match all. */
data class WeightedCarbonReportRecordQuery(
    var category: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCarbonReportRecord): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedCarbonReportRecord>): MutableList<WeightedCarbonReportRecord> {
        val matched = filter(items) { candidate: WeightedCarbonReportRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCarbonReportRecords(store: WeightedCarbonReportRecordStore, query: WeightedCarbonReportRecordQuery): MutableList<WeightedCarbonReportRecord> {
    return query.apply(store.list())
}
