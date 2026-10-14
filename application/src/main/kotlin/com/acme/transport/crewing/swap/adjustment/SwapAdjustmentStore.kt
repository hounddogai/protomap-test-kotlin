package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap adjustments by code. */
interface SwapAdjustmentStore {
    fun find(code: String): SwapAdjustment?
    fun save(item: SwapAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapAdjustmentStore(
    var items: MutableMap<String, SwapAdjustment>,
    var order: MutableList<String>,
) : SwapAdjustmentStore {
    override fun find(code: String): SwapAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapAdjustment) {
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

    override fun list(): MutableList<SwapAdjustment> {
        val out = mutableListOf<SwapAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapAdjustmentStore(): SwapAdjustmentStore {
    return MemorySwapAdjustmentStore(mutableMapOf<String, SwapAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapAdjustmentStore(
    var inner: SwapAdjustmentStore,
    var cache: TtlCache<SwapAdjustment>,
    var hits: Int,
    var misses: Int,
) : SwapAdjustmentStore {
    override fun find(code: String): SwapAdjustment? {
        val hit = (cache.get(code) as SwapAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapAdjustment> = inner.list()
}

fun newCachedSwapAdjustmentStore(inner: SwapAdjustmentStore, ttl: Int, clock: Clock): SwapAdjustmentStore {
    return CachedSwapAdjustmentStore(
        inner = inner,
        cache = newTtlCache<SwapAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap adjustments. Empty fields match all. */
data class SwapAdjustmentQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapAdjustment>): MutableList<SwapAdjustment> {
        val matched = filter(items) { candidate: SwapAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapAdjustments(store: SwapAdjustmentStore, query: SwapAdjustmentQuery): MutableList<SwapAdjustment> {
    return query.apply(store.list())
}
