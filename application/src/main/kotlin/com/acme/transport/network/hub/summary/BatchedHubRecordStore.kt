package com.acme.transport.network.hub.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched hub records by code. */
interface BatchedHubRecordStore {
    fun find(code: String): BatchedHubRecord?
    fun save(item: BatchedHubRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedHubRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedHubRecordStore(
    var items: MutableMap<String, BatchedHubRecord>,
    var order: MutableList<String>,
) : BatchedHubRecordStore {
    override fun find(code: String): BatchedHubRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedHubRecord) {
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

    override fun list(): MutableList<BatchedHubRecord> {
        val out = mutableListOf<BatchedHubRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedHubRecordStore(): BatchedHubRecordStore {
    return MemoryBatchedHubRecordStore(mutableMapOf<String, BatchedHubRecord>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedHubRecordStore(
    var inner: BatchedHubRecordStore,
    var cache: TtlCache<BatchedHubRecord>,
    var hits: Int,
    var misses: Int,
) : BatchedHubRecordStore {
    override fun find(code: String): BatchedHubRecord? {
        val hit = (cache.get(code) as BatchedHubRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedHubRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedHubRecord> = inner.list()
}

fun newCachedBatchedHubRecordStore(inner: BatchedHubRecordStore, ttl: Int, clock: Clock): BatchedHubRecordStore {
    return CachedBatchedHubRecordStore(
        inner = inner,
        cache = newTtlCache<BatchedHubRecord>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched hub records. Empty fields match all. */
data class BatchedHubRecordQuery(
    var category: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedHubRecord): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedHubRecord>): MutableList<BatchedHubRecord> {
        val matched = filter(items) { candidate: BatchedHubRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedHubRecords(store: BatchedHubRecordStore, query: BatchedHubRecordQuery): MutableList<BatchedHubRecord> {
    return query.apply(store.list())
}
