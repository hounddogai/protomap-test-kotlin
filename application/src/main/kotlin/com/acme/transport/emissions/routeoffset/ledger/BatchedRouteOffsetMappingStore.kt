package com.acme.transport.emissions.routeoffset.ledger

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched route offset mappings by code. */
interface BatchedRouteOffsetMappingStore {
    fun find(code: String): BatchedRouteOffsetMapping?
    fun save(item: BatchedRouteOffsetMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRouteOffsetMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRouteOffsetMappingStore(
    var items: MutableMap<String, BatchedRouteOffsetMapping>,
    var order: MutableList<String>,
) : BatchedRouteOffsetMappingStore {
    override fun find(code: String): BatchedRouteOffsetMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRouteOffsetMapping) {
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

    override fun list(): MutableList<BatchedRouteOffsetMapping> {
        val out = mutableListOf<BatchedRouteOffsetMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRouteOffsetMappingStore(): BatchedRouteOffsetMappingStore {
    return MemoryBatchedRouteOffsetMappingStore(
        mutableMapOf<String, BatchedRouteOffsetMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRouteOffsetMappingStore(
    var inner: BatchedRouteOffsetMappingStore,
    var cache: TtlCache<BatchedRouteOffsetMapping>,
    var hits: Int,
    var misses: Int,
) : BatchedRouteOffsetMappingStore {
    override fun find(code: String): BatchedRouteOffsetMapping? {
        val hit = (cache.get(code) as BatchedRouteOffsetMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRouteOffsetMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRouteOffsetMapping> = inner.list()
}

fun newCachedBatchedRouteOffsetMappingStore(inner: BatchedRouteOffsetMappingStore, ttl: Int, clock: Clock): BatchedRouteOffsetMappingStore {
    return CachedBatchedRouteOffsetMappingStore(
        inner = inner,
        cache = newTtlCache<BatchedRouteOffsetMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched route offset mappings. Empty fields match all. */
data class BatchedRouteOffsetMappingQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRouteOffsetMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRouteOffsetMapping>): MutableList<BatchedRouteOffsetMapping> {
        val matched = filter(items) { candidate: BatchedRouteOffsetMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRouteOffsetMappings(store: BatchedRouteOffsetMappingStore, query: BatchedRouteOffsetMappingQuery): MutableList<BatchedRouteOffsetMapping> {
    return query.apply(store.list())
}
