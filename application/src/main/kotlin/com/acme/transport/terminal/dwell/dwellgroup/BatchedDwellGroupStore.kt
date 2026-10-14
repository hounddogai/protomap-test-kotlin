package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched dwell groups by code. */
interface BatchedDwellGroupStore {
    fun find(code: String): BatchedDwellGroup?
    fun save(item: BatchedDwellGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDwellGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDwellGroupStore(
    var items: MutableMap<String, BatchedDwellGroup>,
    var order: MutableList<String>,
) : BatchedDwellGroupStore {
    override fun find(code: String): BatchedDwellGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDwellGroup) {
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

    override fun list(): MutableList<BatchedDwellGroup> {
        val out = mutableListOf<BatchedDwellGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDwellGroupStore(): BatchedDwellGroupStore {
    return MemoryBatchedDwellGroupStore(mutableMapOf<String, BatchedDwellGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDwellGroupStore(
    var inner: BatchedDwellGroupStore,
    var cache: TtlCache<BatchedDwellGroup>,
    var hits: Int,
    var misses: Int,
) : BatchedDwellGroupStore {
    override fun find(code: String): BatchedDwellGroup? {
        val hit = (cache.get(code) as BatchedDwellGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDwellGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDwellGroup> = inner.list()
}

fun newCachedBatchedDwellGroupStore(inner: BatchedDwellGroupStore, ttl: Int, clock: Clock): BatchedDwellGroupStore {
    return CachedBatchedDwellGroupStore(
        inner = inner,
        cache = newTtlCache<BatchedDwellGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched dwell groups. Empty fields match all. */
data class BatchedDwellGroupQuery(
    var source: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDwellGroup): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<BatchedDwellGroup>): MutableList<BatchedDwellGroup> {
        val matched = filter(items) { candidate: BatchedDwellGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDwellGroups(store: BatchedDwellGroupStore, query: BatchedDwellGroupQuery): MutableList<BatchedDwellGroup> {
    return query.apply(store.list())
}
