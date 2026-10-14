package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched accessorial baselines by code. */
interface BatchedAccessorialBaselineStore {
    fun find(code: String): BatchedAccessorialBaseline?
    fun save(item: BatchedAccessorialBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAccessorialBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAccessorialBaselineStore(
    var items: MutableMap<String, BatchedAccessorialBaseline>,
    var order: MutableList<String>,
) : BatchedAccessorialBaselineStore {
    override fun find(code: String): BatchedAccessorialBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAccessorialBaseline) {
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

    override fun list(): MutableList<BatchedAccessorialBaseline> {
        val out = mutableListOf<BatchedAccessorialBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAccessorialBaselineStore(): BatchedAccessorialBaselineStore {
    return MemoryBatchedAccessorialBaselineStore(
        mutableMapOf<String, BatchedAccessorialBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAccessorialBaselineStore(
    var inner: BatchedAccessorialBaselineStore,
    var cache: TtlCache<BatchedAccessorialBaseline>,
    var hits: Int,
    var misses: Int,
) : BatchedAccessorialBaselineStore {
    override fun find(code: String): BatchedAccessorialBaseline? {
        val hit = (cache.get(code) as BatchedAccessorialBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAccessorialBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAccessorialBaseline> = inner.list()
}

fun newCachedBatchedAccessorialBaselineStore(inner: BatchedAccessorialBaselineStore, ttl: Int, clock: Clock): BatchedAccessorialBaselineStore {
    return CachedBatchedAccessorialBaselineStore(
        inner = inner,
        cache = newTtlCache<BatchedAccessorialBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched accessorial baselines. Empty fields match all. */
data class BatchedAccessorialBaselineQuery(
    var category: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAccessorialBaseline): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAccessorialBaseline>): MutableList<BatchedAccessorialBaseline> {
        val matched = filter(items) { candidate: BatchedAccessorialBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAccessorialBaselines(store: BatchedAccessorialBaselineStore, query: BatchedAccessorialBaselineQuery): MutableList<BatchedAccessorialBaseline> {
    return query.apply(store.list())
}
