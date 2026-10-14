package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched rest break records by code. */
interface BatchedRestBreakRecordStore {
    fun find(code: String): BatchedRestBreakRecord?
    fun save(item: BatchedRestBreakRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRestBreakRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRestBreakRecordStore(
    var items: MutableMap<String, BatchedRestBreakRecord>,
    var order: MutableList<String>,
) : BatchedRestBreakRecordStore {
    override fun find(code: String): BatchedRestBreakRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRestBreakRecord) {
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

    override fun list(): MutableList<BatchedRestBreakRecord> {
        val out = mutableListOf<BatchedRestBreakRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRestBreakRecordStore(): BatchedRestBreakRecordStore {
    return MemoryBatchedRestBreakRecordStore(
        mutableMapOf<String, BatchedRestBreakRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRestBreakRecordStore(
    var inner: BatchedRestBreakRecordStore,
    var cache: TtlCache<BatchedRestBreakRecord>,
    var hits: Int,
    var misses: Int,
) : BatchedRestBreakRecordStore {
    override fun find(code: String): BatchedRestBreakRecord? {
        val hit = (cache.get(code) as BatchedRestBreakRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRestBreakRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRestBreakRecord> = inner.list()
}

fun newCachedBatchedRestBreakRecordStore(inner: BatchedRestBreakRecordStore, ttl: Int, clock: Clock): BatchedRestBreakRecordStore {
    return CachedBatchedRestBreakRecordStore(
        inner = inner,
        cache = newTtlCache<BatchedRestBreakRecord>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched rest break records. Empty fields match all. */
data class BatchedRestBreakRecordQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRestBreakRecord): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRestBreakRecord>): MutableList<BatchedRestBreakRecord> {
        val matched = filter(items) { candidate: BatchedRestBreakRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRestBreakRecords(store: BatchedRestBreakRecordStore, query: BatchedRestBreakRecordQuery): MutableList<BatchedRestBreakRecord> {
    return query.apply(store.list())
}
