package com.acme.transport.terminal.sortation.sortationvariance

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched sortation overrides by code. */
interface BatchedSortationOverrideStore {
    fun find(code: String): BatchedSortationOverride?
    fun save(item: BatchedSortationOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedSortationOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedSortationOverrideStore(
    var items: MutableMap<String, BatchedSortationOverride>,
    var order: MutableList<String>,
) : BatchedSortationOverrideStore {
    override fun find(code: String): BatchedSortationOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedSortationOverride) {
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

    override fun list(): MutableList<BatchedSortationOverride> {
        val out = mutableListOf<BatchedSortationOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedSortationOverrideStore(): BatchedSortationOverrideStore {
    return MemoryBatchedSortationOverrideStore(
        mutableMapOf<String, BatchedSortationOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedSortationOverrideStore(
    var inner: BatchedSortationOverrideStore,
    var cache: TtlCache<BatchedSortationOverride>,
    var hits: Int,
    var misses: Int,
) : BatchedSortationOverrideStore {
    override fun find(code: String): BatchedSortationOverride? {
        val hit = (cache.get(code) as BatchedSortationOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedSortationOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedSortationOverride> = inner.list()
}

fun newCachedBatchedSortationOverrideStore(inner: BatchedSortationOverrideStore, ttl: Int, clock: Clock): BatchedSortationOverrideStore {
    return CachedBatchedSortationOverrideStore(
        inner = inner,
        cache = newTtlCache<BatchedSortationOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched sortation overrides. Empty fields match all. */
data class BatchedSortationOverrideQuery(
    var lane: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedSortationOverride): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedSortationOverride>): MutableList<BatchedSortationOverride> {
        val matched = filter(items) { candidate: BatchedSortationOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedSortationOverrides(store: BatchedSortationOverrideStore, query: BatchedSortationOverrideQuery): MutableList<BatchedSortationOverride> {
    return query.apply(store.list())
}
