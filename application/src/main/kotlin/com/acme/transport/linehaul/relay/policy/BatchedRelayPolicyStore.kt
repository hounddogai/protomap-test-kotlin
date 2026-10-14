package com.acme.transport.linehaul.relay.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched relay policies by code. */
interface BatchedRelayPolicyStore {
    fun find(code: String): BatchedRelayPolicy?
    fun save(item: BatchedRelayPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRelayPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRelayPolicyStore(
    var items: MutableMap<String, BatchedRelayPolicy>,
    var order: MutableList<String>,
) : BatchedRelayPolicyStore {
    override fun find(code: String): BatchedRelayPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRelayPolicy) {
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

    override fun list(): MutableList<BatchedRelayPolicy> {
        val out = mutableListOf<BatchedRelayPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRelayPolicyStore(): BatchedRelayPolicyStore {
    return MemoryBatchedRelayPolicyStore(mutableMapOf<String, BatchedRelayPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRelayPolicyStore(
    var inner: BatchedRelayPolicyStore,
    var cache: TtlCache<BatchedRelayPolicy>,
    var hits: Int,
    var misses: Int,
) : BatchedRelayPolicyStore {
    override fun find(code: String): BatchedRelayPolicy? {
        val hit = (cache.get(code) as BatchedRelayPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRelayPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRelayPolicy> = inner.list()
}

fun newCachedBatchedRelayPolicyStore(inner: BatchedRelayPolicyStore, ttl: Int, clock: Clock): BatchedRelayPolicyStore {
    return CachedBatchedRelayPolicyStore(
        inner = inner,
        cache = newTtlCache<BatchedRelayPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched relay policies. Empty fields match all. */
data class BatchedRelayPolicyQuery(
    var category: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRelayPolicy): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRelayPolicy>): MutableList<BatchedRelayPolicy> {
        val matched = filter(items) { candidate: BatchedRelayPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRelayPolicies(store: BatchedRelayPolicyStore, query: BatchedRelayPolicyQuery): MutableList<BatchedRelayPolicy> {
    return query.apply(store.list())
}
