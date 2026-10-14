package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched dock door windows by code. */
interface BatchedDockDoorWindowStore {
    fun find(code: String): BatchedDockDoorWindow?
    fun save(item: BatchedDockDoorWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedDockDoorWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedDockDoorWindowStore(
    var items: MutableMap<String, BatchedDockDoorWindow>,
    var order: MutableList<String>,
) : BatchedDockDoorWindowStore {
    override fun find(code: String): BatchedDockDoorWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedDockDoorWindow) {
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

    override fun list(): MutableList<BatchedDockDoorWindow> {
        val out = mutableListOf<BatchedDockDoorWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedDockDoorWindowStore(): BatchedDockDoorWindowStore {
    return MemoryBatchedDockDoorWindowStore(
        mutableMapOf<String, BatchedDockDoorWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedDockDoorWindowStore(
    var inner: BatchedDockDoorWindowStore,
    var cache: TtlCache<BatchedDockDoorWindow>,
    var hits: Int,
    var misses: Int,
) : BatchedDockDoorWindowStore {
    override fun find(code: String): BatchedDockDoorWindow? {
        val hit = (cache.get(code) as BatchedDockDoorWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedDockDoorWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedDockDoorWindow> = inner.list()
}

fun newCachedBatchedDockDoorWindowStore(inner: BatchedDockDoorWindowStore, ttl: Int, clock: Clock): BatchedDockDoorWindowStore {
    return CachedBatchedDockDoorWindowStore(
        inner = inner,
        cache = newTtlCache<BatchedDockDoorWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched dock door windows. Empty fields match all. */
data class BatchedDockDoorWindowQuery(
    var name: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedDockDoorWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<BatchedDockDoorWindow>): MutableList<BatchedDockDoorWindow> {
        val matched = filter(items) { candidate: BatchedDockDoorWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedDockDoorWindows(store: BatchedDockDoorWindowStore, query: BatchedDockDoorWindowQuery): MutableList<BatchedDockDoorWindow> {
    return query.apply(store.list())
}
