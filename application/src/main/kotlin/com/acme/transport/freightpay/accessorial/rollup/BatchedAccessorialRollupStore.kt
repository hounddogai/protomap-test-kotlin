package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched accessorial rollups by code. */
interface BatchedAccessorialRollupStore {
    fun find(code: String): BatchedAccessorialRollup?
    fun save(item: BatchedAccessorialRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAccessorialRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAccessorialRollupStore(
    var items: MutableMap<String, BatchedAccessorialRollup>,
    var order: MutableList<String>,
) : BatchedAccessorialRollupStore {
    override fun find(code: String): BatchedAccessorialRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAccessorialRollup) {
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

    override fun list(): MutableList<BatchedAccessorialRollup> {
        val out = mutableListOf<BatchedAccessorialRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAccessorialRollupStore(): BatchedAccessorialRollupStore {
    return MemoryBatchedAccessorialRollupStore(
        mutableMapOf<String, BatchedAccessorialRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAccessorialRollupStore(
    var inner: BatchedAccessorialRollupStore,
    var cache: TtlCache<BatchedAccessorialRollup>,
    var hits: Int,
    var misses: Int,
) : BatchedAccessorialRollupStore {
    override fun find(code: String): BatchedAccessorialRollup? {
        val hit = (cache.get(code) as BatchedAccessorialRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAccessorialRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAccessorialRollup> = inner.list()
}

fun newCachedBatchedAccessorialRollupStore(inner: BatchedAccessorialRollupStore, ttl: Int, clock: Clock): BatchedAccessorialRollupStore {
    return CachedBatchedAccessorialRollupStore(
        inner = inner,
        cache = newTtlCache<BatchedAccessorialRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched accessorial rollups. Empty fields match all. */
data class BatchedAccessorialRollupQuery(
    var title: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAccessorialRollup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAccessorialRollup>): MutableList<BatchedAccessorialRollup> {
        val matched = filter(items) { candidate: BatchedAccessorialRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAccessorialRollups(store: BatchedAccessorialRollupStore, query: BatchedAccessorialRollupQuery): MutableList<BatchedAccessorialRollup> {
    return query.apply(store.list())
}
