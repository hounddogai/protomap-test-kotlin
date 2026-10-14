package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched emission factor manifests by code. */
interface BatchedEmissionFactorManifestStore {
    fun find(code: String): BatchedEmissionFactorManifest?
    fun save(item: BatchedEmissionFactorManifest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedEmissionFactorManifest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedEmissionFactorManifestStore(
    var items: MutableMap<String, BatchedEmissionFactorManifest>,
    var order: MutableList<String>,
) : BatchedEmissionFactorManifestStore {
    override fun find(code: String): BatchedEmissionFactorManifest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedEmissionFactorManifest) {
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

    override fun list(): MutableList<BatchedEmissionFactorManifest> {
        val out = mutableListOf<BatchedEmissionFactorManifest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedEmissionFactorManifestStore(): BatchedEmissionFactorManifestStore {
    return MemoryBatchedEmissionFactorManifestStore(
        mutableMapOf<String, BatchedEmissionFactorManifest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedEmissionFactorManifestStore(
    var inner: BatchedEmissionFactorManifestStore,
    var cache: TtlCache<BatchedEmissionFactorManifest>,
    var hits: Int,
    var misses: Int,
) : BatchedEmissionFactorManifestStore {
    override fun find(code: String): BatchedEmissionFactorManifest? {
        val hit = (cache.get(code) as BatchedEmissionFactorManifest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedEmissionFactorManifest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedEmissionFactorManifest> = inner.list()
}

fun newCachedBatchedEmissionFactorManifestStore(inner: BatchedEmissionFactorManifestStore, ttl: Int, clock: Clock): BatchedEmissionFactorManifestStore {
    return CachedBatchedEmissionFactorManifestStore(
        inner = inner,
        cache = newTtlCache<BatchedEmissionFactorManifest>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched emission factor manifests. Empty fields match all. */
data class BatchedEmissionFactorManifestQuery(
    var depot: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedEmissionFactorManifest): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedEmissionFactorManifest>): MutableList<BatchedEmissionFactorManifest> {
        val matched = filter(items) { candidate: BatchedEmissionFactorManifest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedEmissionFactorManifests(store: BatchedEmissionFactorManifestStore, query: BatchedEmissionFactorManifestQuery): MutableList<BatchedEmissionFactorManifest> {
    return query.apply(store.list())
}
