package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection records by code. */
interface InspectionRecordStore {
    fun find(code: String): InspectionRecord?
    fun save(item: InspectionRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionRecordStore(
    var items: MutableMap<String, InspectionRecord>,
    var order: MutableList<String>,
) : InspectionRecordStore {
    override fun find(code: String): InspectionRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionRecord) {
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

    override fun list(): MutableList<InspectionRecord> {
        val out = mutableListOf<InspectionRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionRecordStore(): InspectionRecordStore {
    return MemoryInspectionRecordStore(mutableMapOf<String, InspectionRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionRecordStore(
    var inner: InspectionRecordStore,
    var cache: TtlCache<InspectionRecord>,
    var hits: Int,
    var misses: Int,
) : InspectionRecordStore {
    override fun find(code: String): InspectionRecord? {
        val hit = (cache.get(code) as InspectionRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionRecord> = inner.list()
}

fun newCachedInspectionRecordStore(inner: InspectionRecordStore, ttl: Int, clock: Clock): InspectionRecordStore {
    return CachedInspectionRecordStore(
        inner = inner,
        cache = newTtlCache<InspectionRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection records. Empty fields match all. */
data class InspectionRecordQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionRecord>): MutableList<InspectionRecord> {
        val matched = filter(items) { candidate: InspectionRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionRecords(store: InspectionRecordStore, query: InspectionRecordQuery): MutableList<InspectionRecord> {
    return query.apply(store.list())
}
