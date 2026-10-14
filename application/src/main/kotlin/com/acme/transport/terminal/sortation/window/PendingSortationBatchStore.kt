package com.acme.transport.terminal.sortation.window

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending sortation batches by code. */
interface PendingSortationBatchStore {
    fun find(code: String): PendingSortationBatch?
    fun save(item: PendingSortationBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingSortationBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingSortationBatchStore(
    var items: MutableMap<String, PendingSortationBatch>,
    var order: MutableList<String>,
) : PendingSortationBatchStore {
    override fun find(code: String): PendingSortationBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingSortationBatch) {
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

    override fun list(): MutableList<PendingSortationBatch> {
        val out = mutableListOf<PendingSortationBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingSortationBatchStore(): PendingSortationBatchStore {
    return MemoryPendingSortationBatchStore(
        mutableMapOf<String, PendingSortationBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingSortationBatchStore(
    var inner: PendingSortationBatchStore,
    var cache: TtlCache<PendingSortationBatch>,
    var hits: Int,
    var misses: Int,
) : PendingSortationBatchStore {
    override fun find(code: String): PendingSortationBatch? {
        val hit = (cache.get(code) as PendingSortationBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingSortationBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingSortationBatch> = inner.list()
}

fun newCachedPendingSortationBatchStore(inner: PendingSortationBatchStore, ttl: Int, clock: Clock): PendingSortationBatchStore {
    return CachedPendingSortationBatchStore(
        inner = inner,
        cache = newTtlCache<PendingSortationBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending sortation batches. Empty fields match all. */
data class PendingSortationBatchQuery(
    var region: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingSortationBatch): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingSortationBatch>): MutableList<PendingSortationBatch> {
        val matched = filter(items) { candidate: PendingSortationBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingSortationBatches(store: PendingSortationBatchStore, query: PendingSortationBatchQuery): MutableList<PendingSortationBatch> {
    return query.apply(store.list())
}
