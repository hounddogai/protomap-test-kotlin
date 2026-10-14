package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched fuel burn cycles by code. */
interface BatchedFuelBurnCycleStore {
    fun find(code: String): BatchedFuelBurnCycle?
    fun save(item: BatchedFuelBurnCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedFuelBurnCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedFuelBurnCycleStore(
    var items: MutableMap<String, BatchedFuelBurnCycle>,
    var order: MutableList<String>,
) : BatchedFuelBurnCycleStore {
    override fun find(code: String): BatchedFuelBurnCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedFuelBurnCycle) {
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

    override fun list(): MutableList<BatchedFuelBurnCycle> {
        val out = mutableListOf<BatchedFuelBurnCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedFuelBurnCycleStore(): BatchedFuelBurnCycleStore {
    return MemoryBatchedFuelBurnCycleStore(
        mutableMapOf<String, BatchedFuelBurnCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedFuelBurnCycleStore(
    var inner: BatchedFuelBurnCycleStore,
    var cache: TtlCache<BatchedFuelBurnCycle>,
    var hits: Int,
    var misses: Int,
) : BatchedFuelBurnCycleStore {
    override fun find(code: String): BatchedFuelBurnCycle? {
        val hit = (cache.get(code) as BatchedFuelBurnCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedFuelBurnCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedFuelBurnCycle> = inner.list()
}

fun newCachedBatchedFuelBurnCycleStore(inner: BatchedFuelBurnCycleStore, ttl: Int, clock: Clock): BatchedFuelBurnCycleStore {
    return CachedBatchedFuelBurnCycleStore(
        inner = inner,
        cache = newTtlCache<BatchedFuelBurnCycle>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched fuel burn cycles. Empty fields match all. */
data class BatchedFuelBurnCycleQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedFuelBurnCycle): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedFuelBurnCycle>): MutableList<BatchedFuelBurnCycle> {
        val matched = filter(items) { candidate: BatchedFuelBurnCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedFuelBurnCycles(store: BatchedFuelBurnCycleStore, query: BatchedFuelBurnCycleQuery): MutableList<BatchedFuelBurnCycle> {
    return query.apply(store.list())
}
