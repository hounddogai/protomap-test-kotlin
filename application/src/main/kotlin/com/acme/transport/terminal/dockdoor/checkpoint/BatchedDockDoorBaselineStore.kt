package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched dock door baselines by code. */
interface BatchedDockDoorBaselineStore {
    fun find(code: String): BatchedDockDoorBaseline?
    fun save(item: BatchedDockDoorBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDockDoorBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDockDoorBaselineStore(
    var items: MutableMap<String, BatchedDockDoorBaseline>,
    var order: MutableList<String>,
) : BatchedDockDoorBaselineStore {
    override fun find(code: String): BatchedDockDoorBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDockDoorBaseline) {
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

    override fun list(): MutableList<BatchedDockDoorBaseline> {
        val out = mutableListOf<BatchedDockDoorBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDockDoorBaselineStore(): BatchedDockDoorBaselineStore {
    return MemoryBatchedDockDoorBaselineStore(
        mutableMapOf<String, BatchedDockDoorBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDockDoorBaselineStore(
    var inner: BatchedDockDoorBaselineStore,
    var cache: TtlCache<BatchedDockDoorBaseline>,
    var hits: Int,
    var misses: Int,
) : BatchedDockDoorBaselineStore {
    override fun find(code: String): BatchedDockDoorBaseline? {
        val hit = (cache.get(code) as BatchedDockDoorBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDockDoorBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDockDoorBaseline> = inner.list()
}

fun newCachedBatchedDockDoorBaselineStore(inner: BatchedDockDoorBaselineStore, ttl: Int, clock: Clock): BatchedDockDoorBaselineStore {
    return CachedBatchedDockDoorBaselineStore(
        inner = inner,
        cache = newTtlCache<BatchedDockDoorBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched dock door baselines. Empty fields match all. */
data class BatchedDockDoorBaselineQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDockDoorBaseline): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedDockDoorBaseline>): MutableList<BatchedDockDoorBaseline> {
        val matched = filter(items) { candidate: BatchedDockDoorBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDockDoorBaselines(store: BatchedDockDoorBaselineStore, query: BatchedDockDoorBaselineQuery): MutableList<BatchedDockDoorBaseline> {
    return query.apply(store.list())
}
