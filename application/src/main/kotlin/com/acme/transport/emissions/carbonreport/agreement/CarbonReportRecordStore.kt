package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps carbon report records by code. */
interface CarbonReportRecordStore {
    fun find(code: String): CarbonReportRecord?
    fun save(item: CarbonReportRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<CarbonReportRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCarbonReportRecordStore(
    var items: MutableMap<String, CarbonReportRecord>,
    var order: MutableList<String>,
) : CarbonReportRecordStore {
    override fun find(code: String): CarbonReportRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CarbonReportRecord) {
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

    override fun list(): MutableList<CarbonReportRecord> {
        val out = mutableListOf<CarbonReportRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCarbonReportRecordStore(): CarbonReportRecordStore {
    return MemoryCarbonReportRecordStore(mutableMapOf<String, CarbonReportRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCarbonReportRecordStore(
    var inner: CarbonReportRecordStore,
    var cache: TtlCache<CarbonReportRecord>,
    var hits: Int,
    var misses: Int,
) : CarbonReportRecordStore {
    override fun find(code: String): CarbonReportRecord? {
        val hit = (cache.get(code) as CarbonReportRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CarbonReportRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CarbonReportRecord> = inner.list()
}

fun newCachedCarbonReportRecordStore(inner: CarbonReportRecordStore, ttl: Int, clock: Clock): CarbonReportRecordStore {
    return CachedCarbonReportRecordStore(
        inner = inner,
        cache = newTtlCache<CarbonReportRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects carbon report records. Empty fields match all. */
data class CarbonReportRecordQuery(
    var category: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CarbonReportRecord): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CarbonReportRecord>): MutableList<CarbonReportRecord> {
        val matched = filter(items) { candidate: CarbonReportRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCarbonReportRecords(store: CarbonReportRecordStore, query: CarbonReportRecordQuery): MutableList<CarbonReportRecord> {
    return query.apply(store.list())
}
