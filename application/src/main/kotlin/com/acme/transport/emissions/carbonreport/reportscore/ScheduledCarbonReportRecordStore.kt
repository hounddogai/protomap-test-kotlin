package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled carbon report records by code. */
interface ScheduledCarbonReportRecordStore {
    fun find(code: String): ScheduledCarbonReportRecord?
    fun save(item: ScheduledCarbonReportRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledCarbonReportRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledCarbonReportRecordStore(
    var items: MutableMap<String, ScheduledCarbonReportRecord>,
    var order: MutableList<String>,
) : ScheduledCarbonReportRecordStore {
    override fun find(code: String): ScheduledCarbonReportRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledCarbonReportRecord) {
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

    override fun list(): MutableList<ScheduledCarbonReportRecord> {
        val out = mutableListOf<ScheduledCarbonReportRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledCarbonReportRecordStore(): ScheduledCarbonReportRecordStore {
    return MemoryScheduledCarbonReportRecordStore(
        mutableMapOf<String, ScheduledCarbonReportRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledCarbonReportRecordStore(
    var inner: ScheduledCarbonReportRecordStore,
    var cache: TtlCache<ScheduledCarbonReportRecord>,
    var hits: Int,
    var misses: Int,
) : ScheduledCarbonReportRecordStore {
    override fun find(code: String): ScheduledCarbonReportRecord? {
        val hit = (cache.get(code) as ScheduledCarbonReportRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledCarbonReportRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledCarbonReportRecord> = inner.list()
}

fun newCachedScheduledCarbonReportRecordStore(inner: ScheduledCarbonReportRecordStore, ttl: Int, clock: Clock): ScheduledCarbonReportRecordStore {
    return CachedScheduledCarbonReportRecordStore(
        inner = inner,
        cache = newTtlCache<ScheduledCarbonReportRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled carbon report records. Empty fields match all. */
data class ScheduledCarbonReportRecordQuery(
    var name: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledCarbonReportRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledCarbonReportRecord>): MutableList<ScheduledCarbonReportRecord> {
        val matched = filter(items) { candidate: ScheduledCarbonReportRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledCarbonReportRecords(store: ScheduledCarbonReportRecordStore, query: ScheduledCarbonReportRecordQuery): MutableList<ScheduledCarbonReportRecord> {
    return query.apply(store.list())
}
