package com.acme.transport.emissions.emissionfactor.factorsegment

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted emission factor batches by code. */
interface WeightedEmissionFactorBatchStore {
    fun find(code: String): WeightedEmissionFactorBatch?
    fun save(item: WeightedEmissionFactorBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedEmissionFactorBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedEmissionFactorBatchStore(
    var items: MutableMap<String, WeightedEmissionFactorBatch>,
    var order: MutableList<String>,
) : WeightedEmissionFactorBatchStore {
    override fun find(code: String): WeightedEmissionFactorBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedEmissionFactorBatch) {
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

    override fun list(): MutableList<WeightedEmissionFactorBatch> {
        val out = mutableListOf<WeightedEmissionFactorBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedEmissionFactorBatchStore(): WeightedEmissionFactorBatchStore {
    return MemoryWeightedEmissionFactorBatchStore(
        mutableMapOf<String, WeightedEmissionFactorBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedEmissionFactorBatchStore(
    var inner: WeightedEmissionFactorBatchStore,
    var cache: TtlCache<WeightedEmissionFactorBatch>,
    var hits: Int,
    var misses: Int,
) : WeightedEmissionFactorBatchStore {
    override fun find(code: String): WeightedEmissionFactorBatch? {
        val hit = (cache.get(code) as WeightedEmissionFactorBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedEmissionFactorBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedEmissionFactorBatch> = inner.list()
}

fun newCachedWeightedEmissionFactorBatchStore(inner: WeightedEmissionFactorBatchStore, ttl: Int, clock: Clock): WeightedEmissionFactorBatchStore {
    return CachedWeightedEmissionFactorBatchStore(
        inner = inner,
        cache = newTtlCache<WeightedEmissionFactorBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted emission factor batches. Empty fields match all. */
data class WeightedEmissionFactorBatchQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedEmissionFactorBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedEmissionFactorBatch>): MutableList<WeightedEmissionFactorBatch> {
        val matched = filter(items) { candidate: WeightedEmissionFactorBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedEmissionFactorBatches(store: WeightedEmissionFactorBatchStore, query: WeightedEmissionFactorBatchQuery): MutableList<WeightedEmissionFactorBatch> {
    return query.apply(store.list())
}
