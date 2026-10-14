package com.acme.transport.emissions.emissionfactor.digest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched emission factor digests by code. */
interface BatchedEmissionFactorDigestStore {
    fun find(code: String): BatchedEmissionFactorDigest?
    fun save(item: BatchedEmissionFactorDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedEmissionFactorDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedEmissionFactorDigestStore(
    var items: MutableMap<String, BatchedEmissionFactorDigest>,
    var order: MutableList<String>,
) : BatchedEmissionFactorDigestStore {
    override fun find(code: String): BatchedEmissionFactorDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedEmissionFactorDigest) {
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

    override fun list(): MutableList<BatchedEmissionFactorDigest> {
        val out = mutableListOf<BatchedEmissionFactorDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedEmissionFactorDigestStore(): BatchedEmissionFactorDigestStore {
    return MemoryBatchedEmissionFactorDigestStore(
        mutableMapOf<String, BatchedEmissionFactorDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedEmissionFactorDigestStore(
    var inner: BatchedEmissionFactorDigestStore,
    var cache: TtlCache<BatchedEmissionFactorDigest>,
    var hits: Int,
    var misses: Int,
) : BatchedEmissionFactorDigestStore {
    override fun find(code: String): BatchedEmissionFactorDigest? {
        val hit = (cache.get(code) as BatchedEmissionFactorDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedEmissionFactorDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedEmissionFactorDigest> = inner.list()
}

fun newCachedBatchedEmissionFactorDigestStore(inner: BatchedEmissionFactorDigestStore, ttl: Int, clock: Clock): BatchedEmissionFactorDigestStore {
    return CachedBatchedEmissionFactorDigestStore(
        inner = inner,
        cache = newTtlCache<BatchedEmissionFactorDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched emission factor digests. Empty fields match all. */
data class BatchedEmissionFactorDigestQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedEmissionFactorDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedEmissionFactorDigest>): MutableList<BatchedEmissionFactorDigest> {
        val matched = filter(items) { candidate: BatchedEmissionFactorDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedEmissionFactorDigests(store: BatchedEmissionFactorDigestStore, query: BatchedEmissionFactorDigestQuery): MutableList<BatchedEmissionFactorDigest> {
    return query.apply(store.list())
}
