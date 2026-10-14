package com.acme.transport.crewing.swap.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient swap batches by code. */
interface LenientSwapBatchStore {
    fun find(code: String): LenientSwapBatch?
    fun save(item: LenientSwapBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientSwapBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientSwapBatchStore(
    var items: MutableMap<String, LenientSwapBatch>,
    var order: MutableList<String>,
) : LenientSwapBatchStore {
    override fun find(code: String): LenientSwapBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientSwapBatch) {
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

    override fun list(): MutableList<LenientSwapBatch> {
        val out = mutableListOf<LenientSwapBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientSwapBatchStore(): LenientSwapBatchStore {
    return MemoryLenientSwapBatchStore(mutableMapOf<String, LenientSwapBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientSwapBatchStore(
    var inner: LenientSwapBatchStore,
    var cache: TtlCache<LenientSwapBatch>,
    var hits: Int,
    var misses: Int,
) : LenientSwapBatchStore {
    override fun find(code: String): LenientSwapBatch? {
        val hit = (cache.get(code) as LenientSwapBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientSwapBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientSwapBatch> = inner.list()
}

fun newCachedLenientSwapBatchStore(inner: LenientSwapBatchStore, ttl: Int, clock: Clock): LenientSwapBatchStore {
    return CachedLenientSwapBatchStore(
        inner = inner,
        cache = newTtlCache<LenientSwapBatch>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient swap batches. Empty fields match all. */
data class LenientSwapBatchQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientSwapBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientSwapBatch>): MutableList<LenientSwapBatch> {
        val matched = filter(items) { candidate: LenientSwapBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientSwapBatches(store: LenientSwapBatchStore, query: LenientSwapBatchQuery): MutableList<LenientSwapBatch> {
    return query.apply(store.list())
}
