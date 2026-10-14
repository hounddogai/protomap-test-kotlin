package com.acme.transport.terminal.crossdock.entry

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched cross dock entries by code. */
interface BatchedCrossDockEntryStore {
    fun find(code: String): BatchedCrossDockEntry?
    fun save(item: BatchedCrossDockEntry)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedCrossDockEntry>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedCrossDockEntryStore(
    var items: MutableMap<String, BatchedCrossDockEntry>,
    var order: MutableList<String>,
) : BatchedCrossDockEntryStore {
    override fun find(code: String): BatchedCrossDockEntry? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedCrossDockEntry) {
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

    override fun list(): MutableList<BatchedCrossDockEntry> {
        val out = mutableListOf<BatchedCrossDockEntry>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedCrossDockEntryStore(): BatchedCrossDockEntryStore {
    return MemoryBatchedCrossDockEntryStore(
        mutableMapOf<String, BatchedCrossDockEntry>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedCrossDockEntryStore(
    var inner: BatchedCrossDockEntryStore,
    var cache: TtlCache<BatchedCrossDockEntry>,
    var hits: Int,
    var misses: Int,
) : BatchedCrossDockEntryStore {
    override fun find(code: String): BatchedCrossDockEntry? {
        val hit = (cache.get(code) as BatchedCrossDockEntry?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedCrossDockEntry) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedCrossDockEntry> = inner.list()
}

fun newCachedBatchedCrossDockEntryStore(inner: BatchedCrossDockEntryStore, ttl: Int, clock: Clock): BatchedCrossDockEntryStore {
    return CachedBatchedCrossDockEntryStore(
        inner = inner,
        cache = newTtlCache<BatchedCrossDockEntry>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched cross dock entries. Empty fields match all. */
data class BatchedCrossDockEntryQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedCrossDockEntry): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<BatchedCrossDockEntry>): MutableList<BatchedCrossDockEntry> {
        val matched = filter(items) { candidate: BatchedCrossDockEntry -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedCrossDockEntries(store: BatchedCrossDockEntryStore, query: BatchedCrossDockEntryQuery): MutableList<BatchedCrossDockEntry> {
    return query.apply(store.list())
}
