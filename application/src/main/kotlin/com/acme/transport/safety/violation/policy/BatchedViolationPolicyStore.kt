package com.acme.transport.safety.violation.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched violation policies by code. */
interface BatchedViolationPolicyStore {
    fun find(code: String): BatchedViolationPolicy?
    fun save(item: BatchedViolationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedViolationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedViolationPolicyStore(
    var items: MutableMap<String, BatchedViolationPolicy>,
    var order: MutableList<String>,
) : BatchedViolationPolicyStore {
    override fun find(code: String): BatchedViolationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedViolationPolicy) {
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

    override fun list(): MutableList<BatchedViolationPolicy> {
        val out = mutableListOf<BatchedViolationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedViolationPolicyStore(): BatchedViolationPolicyStore {
    return MemoryBatchedViolationPolicyStore(
        mutableMapOf<String, BatchedViolationPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedViolationPolicyStore(
    var inner: BatchedViolationPolicyStore,
    var cache: TtlCache<BatchedViolationPolicy>,
    var hits: Int,
    var misses: Int,
) : BatchedViolationPolicyStore {
    override fun find(code: String): BatchedViolationPolicy? {
        val hit = (cache.get(code) as BatchedViolationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedViolationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedViolationPolicy> = inner.list()
}

fun newCachedBatchedViolationPolicyStore(inner: BatchedViolationPolicyStore, ttl: Int, clock: Clock): BatchedViolationPolicyStore {
    return CachedBatchedViolationPolicyStore(
        inner = inner,
        cache = newTtlCache<BatchedViolationPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched violation policies. Empty fields match all. */
data class BatchedViolationPolicyQuery(
    var depot: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedViolationPolicy): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedViolationPolicy>): MutableList<BatchedViolationPolicy> {
        val matched = filter(items) { candidate: BatchedViolationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedViolationPolicies(store: BatchedViolationPolicyStore, query: BatchedViolationPolicyQuery): MutableList<BatchedViolationPolicy> {
    return query.apply(store.list())
}
