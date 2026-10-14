package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched dock door records by code. */
interface BatchedDockDoorRecordStore {
    fun find(code: String): BatchedDockDoorRecord?
    fun save(item: BatchedDockDoorRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDockDoorRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDockDoorRecordStore(
    var items: MutableMap<String, BatchedDockDoorRecord>,
    var order: MutableList<String>,
) : BatchedDockDoorRecordStore {
    override fun find(code: String): BatchedDockDoorRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDockDoorRecord) {
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

    override fun list(): MutableList<BatchedDockDoorRecord> {
        val out = mutableListOf<BatchedDockDoorRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDockDoorRecordStore(): BatchedDockDoorRecordStore {
    return MemoryBatchedDockDoorRecordStore(
        mutableMapOf<String, BatchedDockDoorRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDockDoorRecordStore(
    var inner: BatchedDockDoorRecordStore,
    var cache: TtlCache<BatchedDockDoorRecord>,
    var hits: Int,
    var misses: Int,
) : BatchedDockDoorRecordStore {
    override fun find(code: String): BatchedDockDoorRecord? {
        val hit = (cache.get(code) as BatchedDockDoorRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDockDoorRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDockDoorRecord> = inner.list()
}

fun newCachedBatchedDockDoorRecordStore(inner: BatchedDockDoorRecordStore, ttl: Int, clock: Clock): BatchedDockDoorRecordStore {
    return CachedBatchedDockDoorRecordStore(
        inner = inner,
        cache = newTtlCache<BatchedDockDoorRecord>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched dock door records. Empty fields match all. */
data class BatchedDockDoorRecordQuery(
    var country: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDockDoorRecord): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDockDoorRecord>): MutableList<BatchedDockDoorRecord> {
        val matched = filter(items) { candidate: BatchedDockDoorRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDockDoorRecords(store: BatchedDockDoorRecordStore, query: BatchedDockDoorRecordQuery): MutableList<BatchedDockDoorRecord> {
    return query.apply(store.list())
}
