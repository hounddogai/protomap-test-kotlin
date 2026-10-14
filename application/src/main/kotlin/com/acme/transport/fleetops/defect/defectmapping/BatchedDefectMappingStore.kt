package com.acme.transport.fleetops.defect.defectmapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched defect mappings by code. */
interface BatchedDefectMappingStore {
    fun find(code: String): BatchedDefectMapping?
    fun save(item: BatchedDefectMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDefectMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDefectMappingStore(
    var items: MutableMap<String, BatchedDefectMapping>,
    var order: MutableList<String>,
) : BatchedDefectMappingStore {
    override fun find(code: String): BatchedDefectMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDefectMapping) {
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

    override fun list(): MutableList<BatchedDefectMapping> {
        val out = mutableListOf<BatchedDefectMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDefectMappingStore(): BatchedDefectMappingStore {
    return MemoryBatchedDefectMappingStore(
        mutableMapOf<String, BatchedDefectMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDefectMappingStore(
    var inner: BatchedDefectMappingStore,
    var cache: TtlCache<BatchedDefectMapping>,
    var hits: Int,
    var misses: Int,
) : BatchedDefectMappingStore {
    override fun find(code: String): BatchedDefectMapping? {
        val hit = (cache.get(code) as BatchedDefectMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDefectMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDefectMapping> = inner.list()
}

fun newCachedBatchedDefectMappingStore(inner: BatchedDefectMappingStore, ttl: Int, clock: Clock): BatchedDefectMappingStore {
    return CachedBatchedDefectMappingStore(
        inner = inner,
        cache = newTtlCache<BatchedDefectMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched defect mappings. Empty fields match all. */
data class BatchedDefectMappingQuery(
    var lane: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDefectMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDefectMapping>): MutableList<BatchedDefectMapping> {
        val matched = filter(items) { candidate: BatchedDefectMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDefectMappings(store: BatchedDefectMappingStore, query: BatchedDefectMappingQuery): MutableList<BatchedDefectMapping> {
    return query.apply(store.list())
}
