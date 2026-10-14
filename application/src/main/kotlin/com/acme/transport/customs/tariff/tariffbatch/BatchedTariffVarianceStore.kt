package com.acme.transport.customs.tariff.tariffbatch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched tariff variances by code. */
interface BatchedTariffVarianceStore {
    fun find(code: String): BatchedTariffVariance?
    fun save(item: BatchedTariffVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTariffVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTariffVarianceStore(
    var items: MutableMap<String, BatchedTariffVariance>,
    var order: MutableList<String>,
) : BatchedTariffVarianceStore {
    override fun find(code: String): BatchedTariffVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTariffVariance) {
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

    override fun list(): MutableList<BatchedTariffVariance> {
        val out = mutableListOf<BatchedTariffVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTariffVarianceStore(): BatchedTariffVarianceStore {
    return MemoryBatchedTariffVarianceStore(
        mutableMapOf<String, BatchedTariffVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTariffVarianceStore(
    var inner: BatchedTariffVarianceStore,
    var cache: TtlCache<BatchedTariffVariance>,
    var hits: Int,
    var misses: Int,
) : BatchedTariffVarianceStore {
    override fun find(code: String): BatchedTariffVariance? {
        val hit = (cache.get(code) as BatchedTariffVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTariffVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTariffVariance> = inner.list()
}

fun newCachedBatchedTariffVarianceStore(inner: BatchedTariffVarianceStore, ttl: Int, clock: Clock): BatchedTariffVarianceStore {
    return CachedBatchedTariffVarianceStore(
        inner = inner,
        cache = newTtlCache<BatchedTariffVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched tariff variances. Empty fields match all. */
data class BatchedTariffVarianceQuery(
    var source: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTariffVariance): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedTariffVariance>): MutableList<BatchedTariffVariance> {
        val matched = filter(items) { candidate: BatchedTariffVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTariffVariances(store: BatchedTariffVarianceStore, query: BatchedTariffVarianceQuery): MutableList<BatchedTariffVariance> {
    return query.apply(store.list())
}
