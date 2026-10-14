package com.acme.transport.customs.clearance.slot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending clearance records by code. */
interface PendingClearanceRecordStore {
    fun find(code: String): PendingClearanceRecord?
    fun save(item: PendingClearanceRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingClearanceRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingClearanceRecordStore(
    var items: MutableMap<String, PendingClearanceRecord>,
    var order: MutableList<String>,
) : PendingClearanceRecordStore {
    override fun find(code: String): PendingClearanceRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingClearanceRecord) {
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

    override fun list(): MutableList<PendingClearanceRecord> {
        val out = mutableListOf<PendingClearanceRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingClearanceRecordStore(): PendingClearanceRecordStore {
    return MemoryPendingClearanceRecordStore(
        mutableMapOf<String, PendingClearanceRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingClearanceRecordStore(
    var inner: PendingClearanceRecordStore,
    var cache: TtlCache<PendingClearanceRecord>,
    var hits: Int,
    var misses: Int,
) : PendingClearanceRecordStore {
    override fun find(code: String): PendingClearanceRecord? {
        val hit = (cache.get(code) as PendingClearanceRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingClearanceRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingClearanceRecord> = inner.list()
}

fun newCachedPendingClearanceRecordStore(inner: PendingClearanceRecordStore, ttl: Int, clock: Clock): PendingClearanceRecordStore {
    return CachedPendingClearanceRecordStore(
        inner = inner,
        cache = newTtlCache<PendingClearanceRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending clearance records. Empty fields match all. */
data class PendingClearanceRecordQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingClearanceRecord): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingClearanceRecord>): MutableList<PendingClearanceRecord> {
        val matched = filter(items) { candidate: PendingClearanceRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingClearanceRecords(store: PendingClearanceRecordStore, query: PendingClearanceRecordQuery): MutableList<PendingClearanceRecord> {
    return query.apply(store.list())
}
