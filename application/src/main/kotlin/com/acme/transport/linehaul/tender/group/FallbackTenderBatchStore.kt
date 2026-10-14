package com.acme.transport.linehaul.tender.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps fallback tender batches by code. */
interface FallbackTenderBatchStore {
    fun find(code: String): FallbackTenderBatch?
    fun save(item: FallbackTenderBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTenderBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTenderBatchStore(
    var items: MutableMap<String, FallbackTenderBatch>,
    var order: MutableList<String>,
) : FallbackTenderBatchStore {
    override fun find(code: String): FallbackTenderBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTenderBatch) {
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

    override fun list(): MutableList<FallbackTenderBatch> {
        val out = mutableListOf<FallbackTenderBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTenderBatchStore(): FallbackTenderBatchStore {
    return MemoryFallbackTenderBatchStore(mutableMapOf<String, FallbackTenderBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTenderBatchStore(
    var inner: FallbackTenderBatchStore,
    var cache: TtlCache<FallbackTenderBatch>,
    var hits: Int,
    var misses: Int,
) : FallbackTenderBatchStore {
    override fun find(code: String): FallbackTenderBatch? {
        val hit = (cache.get(code) as FallbackTenderBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTenderBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTenderBatch> = inner.list()
}

fun newCachedFallbackTenderBatchStore(inner: FallbackTenderBatchStore, ttl: Int, clock: Clock): FallbackTenderBatchStore {
    return CachedFallbackTenderBatchStore(
        inner = inner,
        cache = newTtlCache<FallbackTenderBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback tender batches. Empty fields match all. */
data class FallbackTenderBatchQuery(
    var name: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTenderBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackTenderBatch>): MutableList<FallbackTenderBatch> {
        val matched = filter(items) { candidate: FallbackTenderBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTenderBatches(store: FallbackTenderBatchStore, query: FallbackTenderBatchQuery): MutableList<FallbackTenderBatch> {
    return query.apply(store.list())
}
