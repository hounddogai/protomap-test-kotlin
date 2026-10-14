package com.acme.transport.fleetops.leasereturn.draft

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched lease return policies by code. */
interface BatchedLeaseReturnPolicyStore {
    fun find(code: String): BatchedLeaseReturnPolicy?
    fun save(item: BatchedLeaseReturnPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedLeaseReturnPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedLeaseReturnPolicyStore(
    var items: MutableMap<String, BatchedLeaseReturnPolicy>,
    var order: MutableList<String>,
) : BatchedLeaseReturnPolicyStore {
    override fun find(code: String): BatchedLeaseReturnPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedLeaseReturnPolicy) {
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

    override fun list(): MutableList<BatchedLeaseReturnPolicy> {
        val out = mutableListOf<BatchedLeaseReturnPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedLeaseReturnPolicyStore(): BatchedLeaseReturnPolicyStore {
    return MemoryBatchedLeaseReturnPolicyStore(
        mutableMapOf<String, BatchedLeaseReturnPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedLeaseReturnPolicyStore(
    var inner: BatchedLeaseReturnPolicyStore,
    var cache: TtlCache<BatchedLeaseReturnPolicy>,
    var hits: Int,
    var misses: Int,
) : BatchedLeaseReturnPolicyStore {
    override fun find(code: String): BatchedLeaseReturnPolicy? {
        val hit = (cache.get(code) as BatchedLeaseReturnPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedLeaseReturnPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedLeaseReturnPolicy> = inner.list()
}

fun newCachedBatchedLeaseReturnPolicyStore(inner: BatchedLeaseReturnPolicyStore, ttl: Int, clock: Clock): BatchedLeaseReturnPolicyStore {
    return CachedBatchedLeaseReturnPolicyStore(
        inner = inner,
        cache = newTtlCache<BatchedLeaseReturnPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched lease return policies. Empty fields match all. */
data class BatchedLeaseReturnPolicyQuery(
    var locale: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedLeaseReturnPolicy): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedLeaseReturnPolicy>): MutableList<BatchedLeaseReturnPolicy> {
        val matched = filter(items) { candidate: BatchedLeaseReturnPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedLeaseReturnPolicies(store: BatchedLeaseReturnPolicyStore, query: BatchedLeaseReturnPolicyQuery): MutableList<BatchedLeaseReturnPolicy> {
    return query.apply(store.list())
}
