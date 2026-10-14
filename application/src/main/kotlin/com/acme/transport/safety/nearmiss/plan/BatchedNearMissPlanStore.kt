package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched near miss plans by code. */
interface BatchedNearMissPlanStore {
    fun find(code: String): BatchedNearMissPlan?
    fun save(item: BatchedNearMissPlan)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedNearMissPlan>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedNearMissPlanStore(
    var items: MutableMap<String, BatchedNearMissPlan>,
    var order: MutableList<String>,
) : BatchedNearMissPlanStore {
    override fun find(code: String): BatchedNearMissPlan? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedNearMissPlan) {
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

    override fun list(): MutableList<BatchedNearMissPlan> {
        val out = mutableListOf<BatchedNearMissPlan>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedNearMissPlanStore(): BatchedNearMissPlanStore {
    return MemoryBatchedNearMissPlanStore(mutableMapOf<String, BatchedNearMissPlan>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedNearMissPlanStore(
    var inner: BatchedNearMissPlanStore,
    var cache: TtlCache<BatchedNearMissPlan>,
    var hits: Int,
    var misses: Int,
) : BatchedNearMissPlanStore {
    override fun find(code: String): BatchedNearMissPlan? {
        val hit = (cache.get(code) as BatchedNearMissPlan?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedNearMissPlan) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedNearMissPlan> = inner.list()
}

fun newCachedBatchedNearMissPlanStore(inner: BatchedNearMissPlanStore, ttl: Int, clock: Clock): BatchedNearMissPlanStore {
    return CachedBatchedNearMissPlanStore(
        inner = inner,
        cache = newTtlCache<BatchedNearMissPlan>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched near miss plans. Empty fields match all. */
data class BatchedNearMissPlanQuery(
    var country: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedNearMissPlan): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedNearMissPlan>): MutableList<BatchedNearMissPlan> {
        val matched = filter(items) { candidate: BatchedNearMissPlan -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedNearMissPlans(store: BatchedNearMissPlanStore, query: BatchedNearMissPlanQuery): MutableList<BatchedNearMissPlan> {
    return query.apply(store.list())
}
