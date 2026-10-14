package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tender batches by code. */
interface TenderBatchStore {
    fun find(code: String): TenderBatch?
    fun save(item: TenderBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<TenderBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTenderBatchStore(
    var items: MutableMap<String, TenderBatch>,
    var order: MutableList<String>,
) : TenderBatchStore {
    override fun find(code: String): TenderBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TenderBatch) {
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

    override fun list(): MutableList<TenderBatch> {
        val out = mutableListOf<TenderBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTenderBatchStore(): TenderBatchStore {
    return MemoryTenderBatchStore(mutableMapOf<String, TenderBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTenderBatchStore(
    var inner: TenderBatchStore,
    var cache: TtlCache<TenderBatch>,
    var hits: Int,
    var misses: Int,
) : TenderBatchStore {
    override fun find(code: String): TenderBatch? {
        val hit = (cache.get(code) as TenderBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TenderBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TenderBatch> = inner.list()
}

fun newCachedTenderBatchStore(inner: TenderBatchStore, ttl: Int, clock: Clock): TenderBatchStore {
    return CachedTenderBatchStore(
        inner = inner,
        cache = newTtlCache<TenderBatch>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tender batches. Empty fields match all. */
data class TenderBatchQuery(
    var name: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TenderBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TenderBatch>): MutableList<TenderBatch> {
        val matched = filter(items) { candidate: TenderBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTenderBatches(store: TenderBatchStore, query: TenderBatchQuery): MutableList<TenderBatch> {
    return query.apply(store.list())
}
