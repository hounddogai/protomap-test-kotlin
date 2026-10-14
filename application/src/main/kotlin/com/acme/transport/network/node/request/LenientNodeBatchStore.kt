package com.acme.transport.network.node.request

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient node batches by code. */
interface LenientNodeBatchStore {
    fun find(code: String): LenientNodeBatch?
    fun save(item: LenientNodeBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientNodeBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientNodeBatchStore(
    var items: MutableMap<String, LenientNodeBatch>,
    var order: MutableList<String>,
) : LenientNodeBatchStore {
    override fun find(code: String): LenientNodeBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientNodeBatch) {
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

    override fun list(): MutableList<LenientNodeBatch> {
        val out = mutableListOf<LenientNodeBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientNodeBatchStore(): LenientNodeBatchStore {
    return MemoryLenientNodeBatchStore(mutableMapOf<String, LenientNodeBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientNodeBatchStore(
    var inner: LenientNodeBatchStore,
    var cache: TtlCache<LenientNodeBatch>,
    var hits: Int,
    var misses: Int,
) : LenientNodeBatchStore {
    override fun find(code: String): LenientNodeBatch? {
        val hit = (cache.get(code) as LenientNodeBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientNodeBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientNodeBatch> = inner.list()
}

fun newCachedLenientNodeBatchStore(inner: LenientNodeBatchStore, ttl: Int, clock: Clock): LenientNodeBatchStore {
    return CachedLenientNodeBatchStore(
        inner = inner,
        cache = newTtlCache<LenientNodeBatch>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient node batches. Empty fields match all. */
data class LenientNodeBatchQuery(
    var source: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientNodeBatch): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<LenientNodeBatch>): MutableList<LenientNodeBatch> {
        val matched = filter(items) { candidate: LenientNodeBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientNodeBatches(store: LenientNodeBatchStore, query: LenientNodeBatchQuery): MutableList<LenientNodeBatch> {
    return query.apply(store.list())
}
