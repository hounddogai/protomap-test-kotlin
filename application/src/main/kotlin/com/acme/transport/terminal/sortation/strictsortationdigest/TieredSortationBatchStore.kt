package com.acme.transport.terminal.sortation.strictsortationdigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered sortation batches by code. */
interface TieredSortationBatchStore {
    fun find(code: String): TieredSortationBatch?
    fun save(item: TieredSortationBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredSortationBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredSortationBatchStore(
    var items: MutableMap<String, TieredSortationBatch>,
    var order: MutableList<String>,
) : TieredSortationBatchStore {
    override fun find(code: String): TieredSortationBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredSortationBatch) {
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

    override fun list(): MutableList<TieredSortationBatch> {
        val out = mutableListOf<TieredSortationBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredSortationBatchStore(): TieredSortationBatchStore {
    return MemoryTieredSortationBatchStore(
        mutableMapOf<String, TieredSortationBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredSortationBatchStore(
    var inner: TieredSortationBatchStore,
    var cache: TtlCache<TieredSortationBatch>,
    var hits: Int,
    var misses: Int,
) : TieredSortationBatchStore {
    override fun find(code: String): TieredSortationBatch? {
        val hit = (cache.get(code) as TieredSortationBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredSortationBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredSortationBatch> = inner.list()
}

fun newCachedTieredSortationBatchStore(inner: TieredSortationBatchStore, ttl: Int, clock: Clock): TieredSortationBatchStore {
    return CachedTieredSortationBatchStore(
        inner = inner,
        cache = newTtlCache<TieredSortationBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered sortation batches. Empty fields match all. */
data class TieredSortationBatchQuery(
    var name: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredSortationBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredSortationBatch>): MutableList<TieredSortationBatch> {
        val matched = filter(items) { candidate: TieredSortationBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredSortationBatches(store: TieredSortationBatchStore, query: TieredSortationBatchQuery): MutableList<TieredSortationBatch> {
    return query.apply(store.list())
}
