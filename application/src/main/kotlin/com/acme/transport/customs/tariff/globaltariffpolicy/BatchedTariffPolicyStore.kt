package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched tariff policies by code. */
interface BatchedTariffPolicyStore {
    fun find(code: String): BatchedTariffPolicy?
    fun save(item: BatchedTariffPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTariffPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTariffPolicyStore(
    var items: MutableMap<String, BatchedTariffPolicy>,
    var order: MutableList<String>,
) : BatchedTariffPolicyStore {
    override fun find(code: String): BatchedTariffPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTariffPolicy) {
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

    override fun list(): MutableList<BatchedTariffPolicy> {
        val out = mutableListOf<BatchedTariffPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTariffPolicyStore(): BatchedTariffPolicyStore {
    return MemoryBatchedTariffPolicyStore(mutableMapOf<String, BatchedTariffPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTariffPolicyStore(
    var inner: BatchedTariffPolicyStore,
    var cache: TtlCache<BatchedTariffPolicy>,
    var hits: Int,
    var misses: Int,
) : BatchedTariffPolicyStore {
    override fun find(code: String): BatchedTariffPolicy? {
        val hit = (cache.get(code) as BatchedTariffPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTariffPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTariffPolicy> = inner.list()
}

fun newCachedBatchedTariffPolicyStore(inner: BatchedTariffPolicyStore, ttl: Int, clock: Clock): BatchedTariffPolicyStore {
    return CachedBatchedTariffPolicyStore(
        inner = inner,
        cache = newTtlCache<BatchedTariffPolicy>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched tariff policies. Empty fields match all. */
data class BatchedTariffPolicyQuery(
    var name: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTariffPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedTariffPolicy>): MutableList<BatchedTariffPolicy> {
        val matched = filter(items) { candidate: BatchedTariffPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTariffPolicies(store: BatchedTariffPolicyStore, query: BatchedTariffPolicyQuery): MutableList<BatchedTariffPolicy> {
    return query.apply(store.list())
}
