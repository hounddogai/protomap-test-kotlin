package com.acme.transport.crewing.availability.cycle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched availability groups by code. */
interface BatchedAvailabilityGroupStore {
    fun find(code: String): BatchedAvailabilityGroup?
    fun save(item: BatchedAvailabilityGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAvailabilityGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAvailabilityGroupStore(
    var items: MutableMap<String, BatchedAvailabilityGroup>,
    var order: MutableList<String>,
) : BatchedAvailabilityGroupStore {
    override fun find(code: String): BatchedAvailabilityGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAvailabilityGroup) {
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

    override fun list(): MutableList<BatchedAvailabilityGroup> {
        val out = mutableListOf<BatchedAvailabilityGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAvailabilityGroupStore(): BatchedAvailabilityGroupStore {
    return MemoryBatchedAvailabilityGroupStore(
        mutableMapOf<String, BatchedAvailabilityGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAvailabilityGroupStore(
    var inner: BatchedAvailabilityGroupStore,
    var cache: TtlCache<BatchedAvailabilityGroup>,
    var hits: Int,
    var misses: Int,
) : BatchedAvailabilityGroupStore {
    override fun find(code: String): BatchedAvailabilityGroup? {
        val hit = (cache.get(code) as BatchedAvailabilityGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAvailabilityGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAvailabilityGroup> = inner.list()
}

fun newCachedBatchedAvailabilityGroupStore(inner: BatchedAvailabilityGroupStore, ttl: Int, clock: Clock): BatchedAvailabilityGroupStore {
    return CachedBatchedAvailabilityGroupStore(
        inner = inner,
        cache = newTtlCache<BatchedAvailabilityGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched availability groups. Empty fields match all. */
data class BatchedAvailabilityGroupQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAvailabilityGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAvailabilityGroup>): MutableList<BatchedAvailabilityGroup> {
        val matched = filter(items) { candidate: BatchedAvailabilityGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAvailabilityGroups(store: BatchedAvailabilityGroupStore, query: BatchedAvailabilityGroupQuery): MutableList<BatchedAvailabilityGroup> {
    return query.apply(store.list())
}
