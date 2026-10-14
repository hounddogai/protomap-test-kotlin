package com.acme.transport.network.node.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched node policies by code. */
interface BatchedNodePolicyStore {
    fun find(code: String): BatchedNodePolicy?
    fun save(item: BatchedNodePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedNodePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedNodePolicyStore(
    var items: MutableMap<String, BatchedNodePolicy>,
    var order: MutableList<String>,
) : BatchedNodePolicyStore {
    override fun find(code: String): BatchedNodePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedNodePolicy) {
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

    override fun list(): MutableList<BatchedNodePolicy> {
        val out = mutableListOf<BatchedNodePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedNodePolicyStore(): BatchedNodePolicyStore {
    return MemoryBatchedNodePolicyStore(mutableMapOf<String, BatchedNodePolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedNodePolicyStore(
    var inner: BatchedNodePolicyStore,
    var cache: TtlCache<BatchedNodePolicy>,
    var hits: Int,
    var misses: Int,
) : BatchedNodePolicyStore {
    override fun find(code: String): BatchedNodePolicy? {
        val hit = (cache.get(code) as BatchedNodePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedNodePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedNodePolicy> = inner.list()
}

fun newCachedBatchedNodePolicyStore(inner: BatchedNodePolicyStore, ttl: Int, clock: Clock): BatchedNodePolicyStore {
    return CachedBatchedNodePolicyStore(
        inner = inner,
        cache = newTtlCache<BatchedNodePolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched node policies. Empty fields match all. */
data class BatchedNodePolicyQuery(
    var label: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedNodePolicy): Boolean {
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

    fun apply(items: MutableList<BatchedNodePolicy>): MutableList<BatchedNodePolicy> {
        val matched = filter(items) { candidate: BatchedNodePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedNodePolicies(store: BatchedNodePolicyStore, query: BatchedNodePolicyQuery): MutableList<BatchedNodePolicy> {
    return query.apply(store.list())
}
