package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched service plan records by code. */
interface BatchedServicePlanRecordStore {
    fun find(code: String): BatchedServicePlanRecord?
    fun save(item: BatchedServicePlanRecord)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedServicePlanRecord>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedServicePlanRecordStore(
    var items: MutableMap<String, BatchedServicePlanRecord>,
    var order: MutableList<String>,
) : BatchedServicePlanRecordStore {
    override fun find(code: String): BatchedServicePlanRecord? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedServicePlanRecord) {
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

    override fun list(): MutableList<BatchedServicePlanRecord> {
        val out = mutableListOf<BatchedServicePlanRecord>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedServicePlanRecordStore(): BatchedServicePlanRecordStore {
    return MemoryBatchedServicePlanRecordStore(
        mutableMapOf<String, BatchedServicePlanRecord>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedServicePlanRecordStore(
    var inner: BatchedServicePlanRecordStore,
    var cache: TtlCache<BatchedServicePlanRecord>,
    var hits: Int,
    var misses: Int,
) : BatchedServicePlanRecordStore {
    override fun find(code: String): BatchedServicePlanRecord? {
        val hit = (cache.get(code) as BatchedServicePlanRecord?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedServicePlanRecord) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedServicePlanRecord> = inner.list()
}

fun newCachedBatchedServicePlanRecordStore(inner: BatchedServicePlanRecordStore, ttl: Int, clock: Clock): BatchedServicePlanRecordStore {
    return CachedBatchedServicePlanRecordStore(
        inner = inner,
        cache = newTtlCache<BatchedServicePlanRecord>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched service plan records. Empty fields match all. */
data class BatchedServicePlanRecordQuery(
    var locale: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedServicePlanRecord): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedServicePlanRecord>): MutableList<BatchedServicePlanRecord> {
        val matched = filter(items) { candidate: BatchedServicePlanRecord -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedServicePlanRecords(store: BatchedServicePlanRecordStore, query: BatchedServicePlanRecordQuery): MutableList<BatchedServicePlanRecord> {
    return query.apply(store.list())
}
