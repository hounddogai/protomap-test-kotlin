package com.acme.transport.crewing.roster.plan

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched roster mappings by code. */
interface BatchedRosterMappingStore {
    fun find(code: String): BatchedRosterMapping?
    fun save(item: BatchedRosterMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRosterMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRosterMappingStore(
    var items: MutableMap<String, BatchedRosterMapping>,
    var order: MutableList<String>,
) : BatchedRosterMappingStore {
    override fun find(code: String): BatchedRosterMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRosterMapping) {
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

    override fun list(): MutableList<BatchedRosterMapping> {
        val out = mutableListOf<BatchedRosterMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRosterMappingStore(): BatchedRosterMappingStore {
    return MemoryBatchedRosterMappingStore(
        mutableMapOf<String, BatchedRosterMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRosterMappingStore(
    var inner: BatchedRosterMappingStore,
    var cache: TtlCache<BatchedRosterMapping>,
    var hits: Int,
    var misses: Int,
) : BatchedRosterMappingStore {
    override fun find(code: String): BatchedRosterMapping? {
        val hit = (cache.get(code) as BatchedRosterMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRosterMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRosterMapping> = inner.list()
}

fun newCachedBatchedRosterMappingStore(inner: BatchedRosterMappingStore, ttl: Int, clock: Clock): BatchedRosterMappingStore {
    return CachedBatchedRosterMappingStore(
        inner = inner,
        cache = newTtlCache<BatchedRosterMapping>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched roster mappings. Empty fields match all. */
data class BatchedRosterMappingQuery(
    var region: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRosterMapping): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRosterMapping>): MutableList<BatchedRosterMapping> {
        val matched = filter(items) { candidate: BatchedRosterMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRosterMappings(store: BatchedRosterMappingStore, query: BatchedRosterMappingQuery): MutableList<BatchedRosterMapping> {
    return query.apply(store.list())
}
