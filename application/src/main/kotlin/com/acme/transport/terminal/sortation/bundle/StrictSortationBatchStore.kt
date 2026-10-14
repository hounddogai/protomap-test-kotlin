package com.acme.transport.terminal.sortation.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict sortation batches by code. */
interface StrictSortationBatchStore {
    fun find(code: String): StrictSortationBatch?
    fun save(item: StrictSortationBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictSortationBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictSortationBatchStore(
    var items: MutableMap<String, StrictSortationBatch>,
    var order: MutableList<String>,
) : StrictSortationBatchStore {
    override fun find(code: String): StrictSortationBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictSortationBatch) {
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

    override fun list(): MutableList<StrictSortationBatch> {
        val out = mutableListOf<StrictSortationBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictSortationBatchStore(): StrictSortationBatchStore {
    return MemoryStrictSortationBatchStore(
        mutableMapOf<String, StrictSortationBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictSortationBatchStore(
    var inner: StrictSortationBatchStore,
    var cache: TtlCache<StrictSortationBatch>,
    var hits: Int,
    var misses: Int,
) : StrictSortationBatchStore {
    override fun find(code: String): StrictSortationBatch? {
        val hit = (cache.get(code) as StrictSortationBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictSortationBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictSortationBatch> = inner.list()
}

fun newCachedStrictSortationBatchStore(inner: StrictSortationBatchStore, ttl: Int, clock: Clock): StrictSortationBatchStore {
    return CachedStrictSortationBatchStore(
        inner = inner,
        cache = newTtlCache<StrictSortationBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict sortation batches. Empty fields match all. */
data class StrictSortationBatchQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictSortationBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictSortationBatch>): MutableList<StrictSortationBatch> {
        val matched = filter(items) { candidate: StrictSortationBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictSortationBatches(store: StrictSortationBatchStore, query: StrictSortationBatchQuery): MutableList<StrictSortationBatch> {
    return query.apply(store.list())
}
