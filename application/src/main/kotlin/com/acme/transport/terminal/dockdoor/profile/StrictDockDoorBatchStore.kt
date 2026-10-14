package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps strict dock door batches by code. */
interface StrictDockDoorBatchStore {
    fun find(code: String): StrictDockDoorBatch?
    fun save(item: StrictDockDoorBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDockDoorBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDockDoorBatchStore(
    var items: MutableMap<String, StrictDockDoorBatch>,
    var order: MutableList<String>,
) : StrictDockDoorBatchStore {
    override fun find(code: String): StrictDockDoorBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDockDoorBatch) {
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

    override fun list(): MutableList<StrictDockDoorBatch> {
        val out = mutableListOf<StrictDockDoorBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDockDoorBatchStore(): StrictDockDoorBatchStore {
    return MemoryStrictDockDoorBatchStore(mutableMapOf<String, StrictDockDoorBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDockDoorBatchStore(
    var inner: StrictDockDoorBatchStore,
    var cache: TtlCache<StrictDockDoorBatch>,
    var hits: Int,
    var misses: Int,
) : StrictDockDoorBatchStore {
    override fun find(code: String): StrictDockDoorBatch? {
        val hit = (cache.get(code) as StrictDockDoorBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDockDoorBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDockDoorBatch> = inner.list()
}

fun newCachedStrictDockDoorBatchStore(inner: StrictDockDoorBatchStore, ttl: Int, clock: Clock): StrictDockDoorBatchStore {
    return CachedStrictDockDoorBatchStore(
        inner = inner,
        cache = newTtlCache<StrictDockDoorBatch>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dock door batches. Empty fields match all. */
data class StrictDockDoorBatchQuery(
    var title: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDockDoorBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDockDoorBatch>): MutableList<StrictDockDoorBatch> {
        val matched = filter(items) { candidate: StrictDockDoorBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDockDoorBatches(store: StrictDockDoorBatchStore, query: StrictDockDoorBatchQuery): MutableList<StrictDockDoorBatch> {
    return query.apply(store.list())
}
