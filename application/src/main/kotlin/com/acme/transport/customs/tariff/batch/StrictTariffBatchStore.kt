package com.acme.transport.customs.tariff.batch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict tariff batches by code. */
interface StrictTariffBatchStore {
    fun find(code: String): StrictTariffBatch?
    fun save(item: StrictTariffBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictTariffBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictTariffBatchStore(
    var items: MutableMap<String, StrictTariffBatch>,
    var order: MutableList<String>,
) : StrictTariffBatchStore {
    override fun find(code: String): StrictTariffBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictTariffBatch) {
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

    override fun list(): MutableList<StrictTariffBatch> {
        val out = mutableListOf<StrictTariffBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictTariffBatchStore(): StrictTariffBatchStore {
    return MemoryStrictTariffBatchStore(mutableMapOf<String, StrictTariffBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictTariffBatchStore(
    var inner: StrictTariffBatchStore,
    var cache: TtlCache<StrictTariffBatch>,
    var hits: Int,
    var misses: Int,
) : StrictTariffBatchStore {
    override fun find(code: String): StrictTariffBatch? {
        val hit = (cache.get(code) as StrictTariffBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictTariffBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictTariffBatch> = inner.list()
}

fun newCachedStrictTariffBatchStore(inner: StrictTariffBatchStore, ttl: Int, clock: Clock): StrictTariffBatchStore {
    return CachedStrictTariffBatchStore(
        inner = inner,
        cache = newTtlCache<StrictTariffBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict tariff batches. Empty fields match all. */
data class StrictTariffBatchQuery(
    var segment: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictTariffBatch): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictTariffBatch>): MutableList<StrictTariffBatch> {
        val matched = filter(items) { candidate: StrictTariffBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictTariffBatches(store: StrictTariffBatchStore, query: StrictTariffBatchQuery): MutableList<StrictTariffBatch> {
    return query.apply(store.list())
}
