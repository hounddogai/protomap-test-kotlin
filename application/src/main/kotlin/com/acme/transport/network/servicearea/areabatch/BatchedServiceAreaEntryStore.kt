package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched service area entries by code. */
interface BatchedServiceAreaEntryStore {
    fun find(code: String): BatchedServiceAreaEntry?
    fun save(item: BatchedServiceAreaEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedServiceAreaEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedServiceAreaEntryStore(
    var items: MutableMap<String, BatchedServiceAreaEntry>,
    var order: MutableList<String>,
) : BatchedServiceAreaEntryStore {
    override fun find(code: String): BatchedServiceAreaEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedServiceAreaEntry) {
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

    override fun list(): MutableList<BatchedServiceAreaEntry> {
        val out = mutableListOf<BatchedServiceAreaEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedServiceAreaEntryStore(): BatchedServiceAreaEntryStore {
    return MemoryBatchedServiceAreaEntryStore(
        mutableMapOf<String, BatchedServiceAreaEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedServiceAreaEntryStore(
    var inner: BatchedServiceAreaEntryStore,
    var cache: TtlCache<BatchedServiceAreaEntry>,
    var hits: Int,
    var misses: Int,
) : BatchedServiceAreaEntryStore {
    override fun find(code: String): BatchedServiceAreaEntry? {
        val hit = (cache.get(code) as BatchedServiceAreaEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedServiceAreaEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedServiceAreaEntry> = inner.list()
}

fun newCachedBatchedServiceAreaEntryStore(inner: BatchedServiceAreaEntryStore, ttl: Int, clock: Clock): BatchedServiceAreaEntryStore {
    return CachedBatchedServiceAreaEntryStore(
        inner = inner,
        cache = newTtlCache<BatchedServiceAreaEntry>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched service area entries. Empty fields match all. */
data class BatchedServiceAreaEntryQuery(
    var segment: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedServiceAreaEntry): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedServiceAreaEntry>): MutableList<BatchedServiceAreaEntry> {
        val matched = filter(items) { candidate: BatchedServiceAreaEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedServiceAreaEntries(store: BatchedServiceAreaEntryStore, query: BatchedServiceAreaEntryQuery): MutableList<BatchedServiceAreaEntry> {
    return query.apply(store.list())
}
