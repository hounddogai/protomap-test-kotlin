package com.acme.transport.customs.clearance.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps clearance records by code. */
interface ClearanceRecordStore {
    fun find(code: String): ClearanceRecord?
    fun save(item: ClearanceRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<ClearanceRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryClearanceRecordStore(
    var items: MutableMap<String, ClearanceRecord>,
    var order: MutableList<String>,
) : ClearanceRecordStore {
    override fun find(code: String): ClearanceRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ClearanceRecord) {
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

    override fun list(): MutableList<ClearanceRecord> {
        val out = mutableListOf<ClearanceRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryClearanceRecordStore(): ClearanceRecordStore {
    return MemoryClearanceRecordStore(mutableMapOf<String, ClearanceRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedClearanceRecordStore(
    var inner: ClearanceRecordStore,
    var cache: TtlCache<ClearanceRecord>,
    var hits: Int,
    var misses: Int,
) : ClearanceRecordStore {
    override fun find(code: String): ClearanceRecord? {
        val hit = (cache.get(code) as ClearanceRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ClearanceRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ClearanceRecord> = inner.list()
}

fun newCachedClearanceRecordStore(inner: ClearanceRecordStore, ttl: Int, clock: Clock): ClearanceRecordStore {
    return CachedClearanceRecordStore(
        inner = inner,
        cache = newTtlCache<ClearanceRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects clearance records. Empty fields match all. */
data class ClearanceRecordQuery(
    var lane: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ClearanceRecord): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ClearanceRecord>): MutableList<ClearanceRecord> {
        val matched = filter(items) { candidate: ClearanceRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findClearanceRecords(store: ClearanceRecordStore, query: ClearanceRecordQuery): MutableList<ClearanceRecord> {
    return query.apply(store.list())
}
