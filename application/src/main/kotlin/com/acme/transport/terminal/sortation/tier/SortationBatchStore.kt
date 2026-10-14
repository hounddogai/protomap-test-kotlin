package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps sortation batches by code. */
interface SortationBatchStore {
    fun find(code: String): SortationBatch?
    fun save(item: SortationBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<SortationBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySortationBatchStore(
    var items: MutableMap<String, SortationBatch>,
    var order: MutableList<String>,
) : SortationBatchStore {
    override fun find(code: String): SortationBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SortationBatch) {
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

    override fun list(): MutableList<SortationBatch> {
        val out = mutableListOf<SortationBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySortationBatchStore(): SortationBatchStore {
    return MemorySortationBatchStore(mutableMapOf<String, SortationBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSortationBatchStore(
    var inner: SortationBatchStore,
    var cache: TtlCache<SortationBatch>,
    var hits: Int,
    var misses: Int,
) : SortationBatchStore {
    override fun find(code: String): SortationBatch? {
        val hit = (cache.get(code) as SortationBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SortationBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SortationBatch> = inner.list()
}

fun newCachedSortationBatchStore(inner: SortationBatchStore, ttl: Int, clock: Clock): SortationBatchStore {
    return CachedSortationBatchStore(
        inner = inner,
        cache = newTtlCache<SortationBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects sortation batches. Empty fields match all. */
data class SortationBatchQuery(
    var name: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SortationBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<SortationBatch>): MutableList<SortationBatch> {
        val matched = filter(items) { candidate: SortationBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSortationBatches(store: SortationBatchStore, query: SortationBatchQuery): MutableList<SortationBatch> {
    return query.apply(store.list())
}
