package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback carbon report records by code. */
interface FallbackCarbonReportRecordStore {
    fun find(code: String): FallbackCarbonReportRecord?
    fun save(item: FallbackCarbonReportRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackCarbonReportRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackCarbonReportRecordStore(
    var items: MutableMap<String, FallbackCarbonReportRecord>,
    var order: MutableList<String>,
) : FallbackCarbonReportRecordStore {
    override fun find(code: String): FallbackCarbonReportRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackCarbonReportRecord) {
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

    override fun list(): MutableList<FallbackCarbonReportRecord> {
        val out = mutableListOf<FallbackCarbonReportRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackCarbonReportRecordStore(): FallbackCarbonReportRecordStore {
    return MemoryFallbackCarbonReportRecordStore(
        mutableMapOf<String, FallbackCarbonReportRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackCarbonReportRecordStore(
    var inner: FallbackCarbonReportRecordStore,
    var cache: TtlCache<FallbackCarbonReportRecord>,
    var hits: Int,
    var misses: Int,
) : FallbackCarbonReportRecordStore {
    override fun find(code: String): FallbackCarbonReportRecord? {
        val hit = (cache.get(code) as FallbackCarbonReportRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackCarbonReportRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackCarbonReportRecord> = inner.list()
}

fun newCachedFallbackCarbonReportRecordStore(inner: FallbackCarbonReportRecordStore, ttl: Int, clock: Clock): FallbackCarbonReportRecordStore {
    return CachedFallbackCarbonReportRecordStore(
        inner = inner,
        cache = newTtlCache<FallbackCarbonReportRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback carbon report records. Empty fields match all. */
data class FallbackCarbonReportRecordQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackCarbonReportRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackCarbonReportRecord>): MutableList<FallbackCarbonReportRecord> {
        val matched = filter(items) { candidate: FallbackCarbonReportRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackCarbonReportRecords(store: FallbackCarbonReportRecordStore, query: FallbackCarbonReportRecordQuery): MutableList<FallbackCarbonReportRecord> {
    return query.apply(store.list())
}
