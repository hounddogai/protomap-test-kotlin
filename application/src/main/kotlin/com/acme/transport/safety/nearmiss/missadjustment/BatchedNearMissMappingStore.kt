package com.acme.transport.safety.nearmiss.missadjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched near miss mappings by code. */
interface BatchedNearMissMappingStore {
    fun find(code: String): BatchedNearMissMapping?
    fun save(item: BatchedNearMissMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedNearMissMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedNearMissMappingStore(
    var items: MutableMap<String, BatchedNearMissMapping>,
    var order: MutableList<String>,
) : BatchedNearMissMappingStore {
    override fun find(code: String): BatchedNearMissMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedNearMissMapping) {
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

    override fun list(): MutableList<BatchedNearMissMapping> {
        val out = mutableListOf<BatchedNearMissMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedNearMissMappingStore(): BatchedNearMissMappingStore {
    return MemoryBatchedNearMissMappingStore(
        mutableMapOf<String, BatchedNearMissMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedNearMissMappingStore(
    var inner: BatchedNearMissMappingStore,
    var cache: TtlCache<BatchedNearMissMapping>,
    var hits: Int,
    var misses: Int,
) : BatchedNearMissMappingStore {
    override fun find(code: String): BatchedNearMissMapping? {
        val hit = (cache.get(code) as BatchedNearMissMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedNearMissMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedNearMissMapping> = inner.list()
}

fun newCachedBatchedNearMissMappingStore(inner: BatchedNearMissMappingStore, ttl: Int, clock: Clock): BatchedNearMissMappingStore {
    return CachedBatchedNearMissMappingStore(
        inner = inner,
        cache = newTtlCache<BatchedNearMissMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched near miss mappings. Empty fields match all. */
data class BatchedNearMissMappingQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedNearMissMapping): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedNearMissMapping>): MutableList<BatchedNearMissMapping> {
        val matched = filter(items) { candidate: BatchedNearMissMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedNearMissMappings(store: BatchedNearMissMappingStore, query: BatchedNearMissMappingQuery): MutableList<BatchedNearMissMapping> {
    return query.apply(store.list())
}
