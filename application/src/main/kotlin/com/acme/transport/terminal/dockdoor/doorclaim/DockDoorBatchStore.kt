package com.acme.transport.terminal.dockdoor.doorclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dock door batches by code. */
interface DockDoorBatchStore {
    fun find(code: String): DockDoorBatch?
    fun save(item: DockDoorBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<DockDoorBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDockDoorBatchStore(
    var items: MutableMap<String, DockDoorBatch>,
    var order: MutableList<String>,
) : DockDoorBatchStore {
    override fun find(code: String): DockDoorBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DockDoorBatch) {
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

    override fun list(): MutableList<DockDoorBatch> {
        val out = mutableListOf<DockDoorBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDockDoorBatchStore(): DockDoorBatchStore {
    return MemoryDockDoorBatchStore(mutableMapOf<String, DockDoorBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDockDoorBatchStore(
    var inner: DockDoorBatchStore,
    var cache: TtlCache<DockDoorBatch>,
    var hits: Int,
    var misses: Int,
) : DockDoorBatchStore {
    override fun find(code: String): DockDoorBatch? {
        val hit = (cache.get(code) as DockDoorBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DockDoorBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DockDoorBatch> = inner.list()
}

fun newCachedDockDoorBatchStore(inner: DockDoorBatchStore, ttl: Int, clock: Clock): DockDoorBatchStore {
    return CachedDockDoorBatchStore(
        inner = inner,
        cache = newTtlCache<DockDoorBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dock door batches. Empty fields match all. */
data class DockDoorBatchQuery(
    var title: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DockDoorBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DockDoorBatch>): MutableList<DockDoorBatch> {
        val matched = filter(items) { candidate: DockDoorBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDockDoorBatches(store: DockDoorBatchStore, query: DockDoorBatchQuery): MutableList<DockDoorBatch> {
    return query.apply(store.list())
}
