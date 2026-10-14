package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched transit time entries by code. */
interface BatchedTransitTimeEntryStore {
    fun find(code: String): BatchedTransitTimeEntry?
    fun save(item: BatchedTransitTimeEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTransitTimeEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTransitTimeEntryStore(
    var items: MutableMap<String, BatchedTransitTimeEntry>,
    var order: MutableList<String>,
) : BatchedTransitTimeEntryStore {
    override fun find(code: String): BatchedTransitTimeEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTransitTimeEntry) {
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

    override fun list(): MutableList<BatchedTransitTimeEntry> {
        val out = mutableListOf<BatchedTransitTimeEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTransitTimeEntryStore(): BatchedTransitTimeEntryStore {
    return MemoryBatchedTransitTimeEntryStore(
        mutableMapOf<String, BatchedTransitTimeEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTransitTimeEntryStore(
    var inner: BatchedTransitTimeEntryStore,
    var cache: TtlCache<BatchedTransitTimeEntry>,
    var hits: Int,
    var misses: Int,
) : BatchedTransitTimeEntryStore {
    override fun find(code: String): BatchedTransitTimeEntry? {
        val hit = (cache.get(code) as BatchedTransitTimeEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTransitTimeEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTransitTimeEntry> = inner.list()
}

fun newCachedBatchedTransitTimeEntryStore(inner: BatchedTransitTimeEntryStore, ttl: Int, clock: Clock): BatchedTransitTimeEntryStore {
    return CachedBatchedTransitTimeEntryStore(
        inner = inner,
        cache = newTtlCache<BatchedTransitTimeEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched transit time entries. Empty fields match all. */
data class BatchedTransitTimeEntryQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTransitTimeEntry): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<BatchedTransitTimeEntry>): MutableList<BatchedTransitTimeEntry> {
        val matched = filter(items) { candidate: BatchedTransitTimeEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTransitTimeEntries(store: BatchedTransitTimeEntryStore, query: BatchedTransitTimeEntryQuery): MutableList<BatchedTransitTimeEntry> {
    return query.apply(store.list())
}
