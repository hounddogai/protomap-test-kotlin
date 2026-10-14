package com.acme.transport.network.node.tier

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched node baselines by code. */
interface BatchedNodeBaselineStore {
    fun find(code: String): BatchedNodeBaseline?
    fun save(item: BatchedNodeBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedNodeBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedNodeBaselineStore(
    var items: MutableMap<String, BatchedNodeBaseline>,
    var order: MutableList<String>,
) : BatchedNodeBaselineStore {
    override fun find(code: String): BatchedNodeBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedNodeBaseline) {
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

    override fun list(): MutableList<BatchedNodeBaseline> {
        val out = mutableListOf<BatchedNodeBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedNodeBaselineStore(): BatchedNodeBaselineStore {
    return MemoryBatchedNodeBaselineStore(mutableMapOf<String, BatchedNodeBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedNodeBaselineStore(
    var inner: BatchedNodeBaselineStore,
    var cache: TtlCache<BatchedNodeBaseline>,
    var hits: Int,
    var misses: Int,
) : BatchedNodeBaselineStore {
    override fun find(code: String): BatchedNodeBaseline? {
        val hit = (cache.get(code) as BatchedNodeBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedNodeBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedNodeBaseline> = inner.list()
}

fun newCachedBatchedNodeBaselineStore(inner: BatchedNodeBaselineStore, ttl: Int, clock: Clock): BatchedNodeBaselineStore {
    return CachedBatchedNodeBaselineStore(
        inner = inner,
        cache = newTtlCache<BatchedNodeBaseline>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched node baselines. Empty fields match all. */
data class BatchedNodeBaselineQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedNodeBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<BatchedNodeBaseline>): MutableList<BatchedNodeBaseline> {
        val matched = filter(items) { candidate: BatchedNodeBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedNodeBaselines(store: BatchedNodeBaselineStore, query: BatchedNodeBaselineQuery): MutableList<BatchedNodeBaseline> {
    return query.apply(store.list())
}
