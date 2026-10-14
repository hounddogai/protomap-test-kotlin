package com.acme.transport.network.zone.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched zone records by code. */
interface BatchedZoneRecordStore {
    fun find(code: String): BatchedZoneRecord?
    fun save(item: BatchedZoneRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedZoneRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedZoneRecordStore(
    var items: MutableMap<String, BatchedZoneRecord>,
    var order: MutableList<String>,
) : BatchedZoneRecordStore {
    override fun find(code: String): BatchedZoneRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedZoneRecord) {
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

    override fun list(): MutableList<BatchedZoneRecord> {
        val out = mutableListOf<BatchedZoneRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedZoneRecordStore(): BatchedZoneRecordStore {
    return MemoryBatchedZoneRecordStore(mutableMapOf<String, BatchedZoneRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedZoneRecordStore(
    var inner: BatchedZoneRecordStore,
    var cache: TtlCache<BatchedZoneRecord>,
    var hits: Int,
    var misses: Int,
) : BatchedZoneRecordStore {
    override fun find(code: String): BatchedZoneRecord? {
        val hit = (cache.get(code) as BatchedZoneRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedZoneRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedZoneRecord> = inner.list()
}

fun newCachedBatchedZoneRecordStore(inner: BatchedZoneRecordStore, ttl: Int, clock: Clock): BatchedZoneRecordStore {
    return CachedBatchedZoneRecordStore(
        inner = inner,
        cache = newTtlCache<BatchedZoneRecord>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched zone records. Empty fields match all. */
data class BatchedZoneRecordQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedZoneRecord): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedZoneRecord>): MutableList<BatchedZoneRecord> {
        val matched = filter(items) { candidate: BatchedZoneRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedZoneRecords(store: BatchedZoneRecordStore, query: BatchedZoneRecordQuery): MutableList<BatchedZoneRecord> {
    return query.apply(store.list())
}
