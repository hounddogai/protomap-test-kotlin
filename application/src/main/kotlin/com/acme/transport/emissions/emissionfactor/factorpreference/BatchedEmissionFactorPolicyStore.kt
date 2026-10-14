package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched emission factor policies by code. */
interface BatchedEmissionFactorPolicyStore {
    fun find(code: String): BatchedEmissionFactorPolicy?
    fun save(item: BatchedEmissionFactorPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedEmissionFactorPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedEmissionFactorPolicyStore(
    var items: MutableMap<String, BatchedEmissionFactorPolicy>,
    var order: MutableList<String>,
) : BatchedEmissionFactorPolicyStore {
    override fun find(code: String): BatchedEmissionFactorPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedEmissionFactorPolicy) {
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

    override fun list(): MutableList<BatchedEmissionFactorPolicy> {
        val out = mutableListOf<BatchedEmissionFactorPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedEmissionFactorPolicyStore(): BatchedEmissionFactorPolicyStore {
    return MemoryBatchedEmissionFactorPolicyStore(
        mutableMapOf<String, BatchedEmissionFactorPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedEmissionFactorPolicyStore(
    var inner: BatchedEmissionFactorPolicyStore,
    var cache: TtlCache<BatchedEmissionFactorPolicy>,
    var hits: Int,
    var misses: Int,
) : BatchedEmissionFactorPolicyStore {
    override fun find(code: String): BatchedEmissionFactorPolicy? {
        val hit = (cache.get(code) as BatchedEmissionFactorPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedEmissionFactorPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedEmissionFactorPolicy> = inner.list()
}

fun newCachedBatchedEmissionFactorPolicyStore(inner: BatchedEmissionFactorPolicyStore, ttl: Int, clock: Clock): BatchedEmissionFactorPolicyStore {
    return CachedBatchedEmissionFactorPolicyStore(
        inner = inner,
        cache = newTtlCache<BatchedEmissionFactorPolicy>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched emission factor policies. Empty fields match all. */
data class BatchedEmissionFactorPolicyQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedEmissionFactorPolicy): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedEmissionFactorPolicy>): MutableList<BatchedEmissionFactorPolicy> {
        val matched = filter(items) { candidate: BatchedEmissionFactorPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedEmissionFactorPolicies(store: BatchedEmissionFactorPolicyStore, query: BatchedEmissionFactorPolicyQuery): MutableList<BatchedEmissionFactorPolicy> {
    return query.apply(store.list())
}
