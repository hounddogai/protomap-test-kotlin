package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched tariff rollups by code. */
interface BatchedTariffRollupStore {
    fun find(code: String): BatchedTariffRollup?
    fun save(item: BatchedTariffRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTariffRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTariffRollupStore(
    var items: MutableMap<String, BatchedTariffRollup>,
    var order: MutableList<String>,
) : BatchedTariffRollupStore {
    override fun find(code: String): BatchedTariffRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTariffRollup) {
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

    override fun list(): MutableList<BatchedTariffRollup> {
        val out = mutableListOf<BatchedTariffRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTariffRollupStore(): BatchedTariffRollupStore {
    return MemoryBatchedTariffRollupStore(mutableMapOf<String, BatchedTariffRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTariffRollupStore(
    var inner: BatchedTariffRollupStore,
    var cache: TtlCache<BatchedTariffRollup>,
    var hits: Int,
    var misses: Int,
) : BatchedTariffRollupStore {
    override fun find(code: String): BatchedTariffRollup? {
        val hit = (cache.get(code) as BatchedTariffRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTariffRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTariffRollup> = inner.list()
}

fun newCachedBatchedTariffRollupStore(inner: BatchedTariffRollupStore, ttl: Int, clock: Clock): BatchedTariffRollupStore {
    return CachedBatchedTariffRollupStore(
        inner = inner,
        cache = newTtlCache<BatchedTariffRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched tariff rollups. Empty fields match all. */
data class BatchedTariffRollupQuery(
    var region: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTariffRollup): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedTariffRollup>): MutableList<BatchedTariffRollup> {
        val matched = filter(items) { candidate: BatchedTariffRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTariffRollups(store: BatchedTariffRollupStore, query: BatchedTariffRollupQuery): MutableList<BatchedTariffRollup> {
    return query.apply(store.list())
}
