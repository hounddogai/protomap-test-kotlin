package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered emission factor batches by code. */
interface TieredEmissionFactorBatchStore {
    fun find(code: String): TieredEmissionFactorBatch?
    fun save(item: TieredEmissionFactorBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredEmissionFactorBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredEmissionFactorBatchStore(
    var items: MutableMap<String, TieredEmissionFactorBatch>,
    var order: MutableList<String>,
) : TieredEmissionFactorBatchStore {
    override fun find(code: String): TieredEmissionFactorBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredEmissionFactorBatch) {
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

    override fun list(): MutableList<TieredEmissionFactorBatch> {
        val out = mutableListOf<TieredEmissionFactorBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredEmissionFactorBatchStore(): TieredEmissionFactorBatchStore {
    return MemoryTieredEmissionFactorBatchStore(
        mutableMapOf<String, TieredEmissionFactorBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredEmissionFactorBatchStore(
    var inner: TieredEmissionFactorBatchStore,
    var cache: TtlCache<TieredEmissionFactorBatch>,
    var hits: Int,
    var misses: Int,
) : TieredEmissionFactorBatchStore {
    override fun find(code: String): TieredEmissionFactorBatch? {
        val hit = (cache.get(code) as TieredEmissionFactorBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredEmissionFactorBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredEmissionFactorBatch> = inner.list()
}

fun newCachedTieredEmissionFactorBatchStore(inner: TieredEmissionFactorBatchStore, ttl: Int, clock: Clock): TieredEmissionFactorBatchStore {
    return CachedTieredEmissionFactorBatchStore(
        inner = inner,
        cache = newTtlCache<TieredEmissionFactorBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered emission factor batches. Empty fields match all. */
data class TieredEmissionFactorBatchQuery(
    var depot: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredEmissionFactorBatch): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredEmissionFactorBatch>): MutableList<TieredEmissionFactorBatch> {
        val matched = filter(items) { candidate: TieredEmissionFactorBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredEmissionFactorBatches(store: TieredEmissionFactorBatchStore, query: TieredEmissionFactorBatchQuery): MutableList<TieredEmissionFactorBatch> {
    return query.apply(store.list())
}
