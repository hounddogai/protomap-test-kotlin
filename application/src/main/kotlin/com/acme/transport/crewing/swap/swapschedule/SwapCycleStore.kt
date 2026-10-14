package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap cycles by code. */
interface SwapCycleStore {
    fun find(code: String): SwapCycle?
    fun save(item: SwapCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapCycleStore(
    var items: MutableMap<String, SwapCycle>,
    var order: MutableList<String>,
) : SwapCycleStore {
    override fun find(code: String): SwapCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapCycle) {
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

    override fun list(): MutableList<SwapCycle> {
        val out = mutableListOf<SwapCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapCycleStore(): SwapCycleStore {
    return MemorySwapCycleStore(mutableMapOf<String, SwapCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapCycleStore(
    var inner: SwapCycleStore,
    var cache: TtlCache<SwapCycle>,
    var hits: Int,
    var misses: Int,
) : SwapCycleStore {
    override fun find(code: String): SwapCycle? {
        val hit = (cache.get(code) as SwapCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapCycle> = inner.list()
}

fun newCachedSwapCycleStore(inner: SwapCycleStore, ttl: Int, clock: Clock): SwapCycleStore {
    return CachedSwapCycleStore(
        inner = inner,
        cache = newTtlCache<SwapCycle>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap cycles. Empty fields match all. */
data class SwapCycleQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapCycle): Boolean {
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

    fun apply(items: MutableList<SwapCycle>): MutableList<SwapCycle> {
        val matched = filter(items) { candidate: SwapCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapCycles(store: SwapCycleStore, query: SwapCycleQuery): MutableList<SwapCycle> {
    return query.apply(store.list())
}
