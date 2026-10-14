package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched rest break thresholds by code. */
interface BatchedRestBreakThresholdStore {
    fun find(code: String): BatchedRestBreakThreshold?
    fun save(item: BatchedRestBreakThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRestBreakThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRestBreakThresholdStore(
    var items: MutableMap<String, BatchedRestBreakThreshold>,
    var order: MutableList<String>,
) : BatchedRestBreakThresholdStore {
    override fun find(code: String): BatchedRestBreakThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRestBreakThreshold) {
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

    override fun list(): MutableList<BatchedRestBreakThreshold> {
        val out = mutableListOf<BatchedRestBreakThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRestBreakThresholdStore(): BatchedRestBreakThresholdStore {
    return MemoryBatchedRestBreakThresholdStore(
        mutableMapOf<String, BatchedRestBreakThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRestBreakThresholdStore(
    var inner: BatchedRestBreakThresholdStore,
    var cache: TtlCache<BatchedRestBreakThreshold>,
    var hits: Int,
    var misses: Int,
) : BatchedRestBreakThresholdStore {
    override fun find(code: String): BatchedRestBreakThreshold? {
        val hit = (cache.get(code) as BatchedRestBreakThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRestBreakThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRestBreakThreshold> = inner.list()
}

fun newCachedBatchedRestBreakThresholdStore(inner: BatchedRestBreakThresholdStore, ttl: Int, clock: Clock): BatchedRestBreakThresholdStore {
    return CachedBatchedRestBreakThresholdStore(
        inner = inner,
        cache = newTtlCache<BatchedRestBreakThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched rest break thresholds. Empty fields match all. */
data class BatchedRestBreakThresholdQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRestBreakThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRestBreakThreshold>): MutableList<BatchedRestBreakThreshold> {
        val matched = filter(items) { candidate: BatchedRestBreakThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRestBreakThresholds(store: BatchedRestBreakThresholdStore, query: BatchedRestBreakThresholdQuery): MutableList<BatchedRestBreakThreshold> {
    return query.apply(store.list())
}
