package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect records by code. */
interface DefectRecordStore {
    fun find(code: String): DefectRecord?
    fun save(item: DefectRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectRecordStore(
    var items: MutableMap<String, DefectRecord>,
    var order: MutableList<String>,
) : DefectRecordStore {
    override fun find(code: String): DefectRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectRecord) {
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

    override fun list(): MutableList<DefectRecord> {
        val out = mutableListOf<DefectRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectRecordStore(): DefectRecordStore {
    return MemoryDefectRecordStore(mutableMapOf<String, DefectRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectRecordStore(
    var inner: DefectRecordStore,
    var cache: TtlCache<DefectRecord>,
    var hits: Int,
    var misses: Int,
) : DefectRecordStore {
    override fun find(code: String): DefectRecord? {
        val hit = (cache.get(code) as DefectRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectRecord> = inner.list()
}

fun newCachedDefectRecordStore(inner: DefectRecordStore, ttl: Int, clock: Clock): DefectRecordStore {
    return CachedDefectRecordStore(
        inner = inner,
        cache = newTtlCache<DefectRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect records. Empty fields match all. */
data class DefectRecordQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectRecord): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefectRecord>): MutableList<DefectRecord> {
        val matched = filter(items) { candidate: DefectRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectRecords(store: DefectRecordStore, query: DefectRecordQuery): MutableList<DefectRecord> {
    return query.apply(store.list())
}
