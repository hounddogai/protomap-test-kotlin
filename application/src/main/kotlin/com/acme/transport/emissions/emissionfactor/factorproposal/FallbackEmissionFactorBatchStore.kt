package com.acme.transport.emissions.emissionfactor.factorproposal

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback emission factor batches by code. */
interface FallbackEmissionFactorBatchStore {
    fun find(code: String): FallbackEmissionFactorBatch?
    fun save(item: FallbackEmissionFactorBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEmissionFactorBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEmissionFactorBatchStore(
    var items: MutableMap<String, FallbackEmissionFactorBatch>,
    var order: MutableList<String>,
) : FallbackEmissionFactorBatchStore {
    override fun find(code: String): FallbackEmissionFactorBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEmissionFactorBatch) {
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

    override fun list(): MutableList<FallbackEmissionFactorBatch> {
        val out = mutableListOf<FallbackEmissionFactorBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEmissionFactorBatchStore(): FallbackEmissionFactorBatchStore {
    return MemoryFallbackEmissionFactorBatchStore(
        mutableMapOf<String, FallbackEmissionFactorBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEmissionFactorBatchStore(
    var inner: FallbackEmissionFactorBatchStore,
    var cache: TtlCache<FallbackEmissionFactorBatch>,
    var hits: Int,
    var misses: Int,
) : FallbackEmissionFactorBatchStore {
    override fun find(code: String): FallbackEmissionFactorBatch? {
        val hit = (cache.get(code) as FallbackEmissionFactorBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEmissionFactorBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEmissionFactorBatch> = inner.list()
}

fun newCachedFallbackEmissionFactorBatchStore(inner: FallbackEmissionFactorBatchStore, ttl: Int, clock: Clock): FallbackEmissionFactorBatchStore {
    return CachedFallbackEmissionFactorBatchStore(
        inner = inner,
        cache = newTtlCache<FallbackEmissionFactorBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback emission factor batches. Empty fields match all. */
data class FallbackEmissionFactorBatchQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEmissionFactorBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackEmissionFactorBatch>): MutableList<FallbackEmissionFactorBatch> {
        val matched = filter(items) { candidate: FallbackEmissionFactorBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEmissionFactorBatches(store: FallbackEmissionFactorBatchStore, query: FallbackEmissionFactorBatchQuery): MutableList<FallbackEmissionFactorBatch> {
    return query.apply(store.list())
}
