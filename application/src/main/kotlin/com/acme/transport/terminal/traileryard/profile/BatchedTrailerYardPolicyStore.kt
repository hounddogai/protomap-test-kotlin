package com.acme.transport.terminal.traileryard.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched trailer yard policies by code. */
interface BatchedTrailerYardPolicyStore {
    fun find(code: String): BatchedTrailerYardPolicy?
    fun save(item: BatchedTrailerYardPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTrailerYardPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTrailerYardPolicyStore(
    var items: MutableMap<String, BatchedTrailerYardPolicy>,
    var order: MutableList<String>,
) : BatchedTrailerYardPolicyStore {
    override fun find(code: String): BatchedTrailerYardPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTrailerYardPolicy) {
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

    override fun list(): MutableList<BatchedTrailerYardPolicy> {
        val out = mutableListOf<BatchedTrailerYardPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTrailerYardPolicyStore(): BatchedTrailerYardPolicyStore {
    return MemoryBatchedTrailerYardPolicyStore(
        mutableMapOf<String, BatchedTrailerYardPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTrailerYardPolicyStore(
    var inner: BatchedTrailerYardPolicyStore,
    var cache: TtlCache<BatchedTrailerYardPolicy>,
    var hits: Int,
    var misses: Int,
) : BatchedTrailerYardPolicyStore {
    override fun find(code: String): BatchedTrailerYardPolicy? {
        val hit = (cache.get(code) as BatchedTrailerYardPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTrailerYardPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTrailerYardPolicy> = inner.list()
}

fun newCachedBatchedTrailerYardPolicyStore(inner: BatchedTrailerYardPolicyStore, ttl: Int, clock: Clock): BatchedTrailerYardPolicyStore {
    return CachedBatchedTrailerYardPolicyStore(
        inner = inner,
        cache = newTtlCache<BatchedTrailerYardPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched trailer yard policies. Empty fields match all. */
data class BatchedTrailerYardPolicyQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTrailerYardPolicy): Boolean {
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

    fun apply(items: MutableList<BatchedTrailerYardPolicy>): MutableList<BatchedTrailerYardPolicy> {
        val matched = filter(items) { candidate: BatchedTrailerYardPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTrailerYardPolicies(store: BatchedTrailerYardPolicyStore, query: BatchedTrailerYardPolicyQuery): MutableList<BatchedTrailerYardPolicy> {
    return query.apply(store.list())
}
