package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered defect records by code. */
interface TieredDefectRecordStore {
    fun find(code: String): TieredDefectRecord?
    fun save(item: TieredDefectRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDefectRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDefectRecordStore(
    var items: MutableMap<String, TieredDefectRecord>,
    var order: MutableList<String>,
) : TieredDefectRecordStore {
    override fun find(code: String): TieredDefectRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDefectRecord) {
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

    override fun list(): MutableList<TieredDefectRecord> {
        val out = mutableListOf<TieredDefectRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDefectRecordStore(): TieredDefectRecordStore {
    return MemoryTieredDefectRecordStore(mutableMapOf<String, TieredDefectRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDefectRecordStore(
    var inner: TieredDefectRecordStore,
    var cache: TtlCache<TieredDefectRecord>,
    var hits: Int,
    var misses: Int,
) : TieredDefectRecordStore {
    override fun find(code: String): TieredDefectRecord? {
        val hit = (cache.get(code) as TieredDefectRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDefectRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDefectRecord> = inner.list()
}

fun newCachedTieredDefectRecordStore(inner: TieredDefectRecordStore, ttl: Int, clock: Clock): TieredDefectRecordStore {
    return CachedTieredDefectRecordStore(
        inner = inner,
        cache = newTtlCache<TieredDefectRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered defect records. Empty fields match all. */
data class TieredDefectRecordQuery(
    var category: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDefectRecord): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<TieredDefectRecord>): MutableList<TieredDefectRecord> {
        val matched = filter(items) { candidate: TieredDefectRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDefectRecords(store: TieredDefectRecordStore, query: TieredDefectRecordQuery): MutableList<TieredDefectRecord> {
    return query.apply(store.list())
}
