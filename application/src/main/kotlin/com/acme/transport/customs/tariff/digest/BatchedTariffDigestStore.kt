package com.acme.transport.customs.tariff.digest

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched tariff digests by code. */
interface BatchedTariffDigestStore {
    fun find(code: String): BatchedTariffDigest?
    fun save(item: BatchedTariffDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTariffDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTariffDigestStore(
    var items: MutableMap<String, BatchedTariffDigest>,
    var order: MutableList<String>,
) : BatchedTariffDigestStore {
    override fun find(code: String): BatchedTariffDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTariffDigest) {
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

    override fun list(): MutableList<BatchedTariffDigest> {
        val out = mutableListOf<BatchedTariffDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTariffDigestStore(): BatchedTariffDigestStore {
    return MemoryBatchedTariffDigestStore(mutableMapOf<String, BatchedTariffDigest>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTariffDigestStore(
    var inner: BatchedTariffDigestStore,
    var cache: TtlCache<BatchedTariffDigest>,
    var hits: Int,
    var misses: Int,
) : BatchedTariffDigestStore {
    override fun find(code: String): BatchedTariffDigest? {
        val hit = (cache.get(code) as BatchedTariffDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTariffDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTariffDigest> = inner.list()
}

fun newCachedBatchedTariffDigestStore(inner: BatchedTariffDigestStore, ttl: Int, clock: Clock): BatchedTariffDigestStore {
    return CachedBatchedTariffDigestStore(
        inner = inner,
        cache = newTtlCache<BatchedTariffDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched tariff digests. Empty fields match all. */
data class BatchedTariffDigestQuery(
    var locale: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTariffDigest): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
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

    fun apply(items: MutableList<BatchedTariffDigest>): MutableList<BatchedTariffDigest> {
        val matched = filter(items) { candidate: BatchedTariffDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTariffDigests(store: BatchedTariffDigestStore, query: BatchedTariffDigestQuery): MutableList<BatchedTariffDigest> {
    return query.apply(store.list())
}
