package com.acme.transport.fleetops.defect.budget

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict defect batches by code. */
interface StrictDefectBatchStore {
    fun find(code: String): StrictDefectBatch?
    fun save(item: StrictDefectBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDefectBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDefectBatchStore(
    var items: MutableMap<String, StrictDefectBatch>,
    var order: MutableList<String>,
) : StrictDefectBatchStore {
    override fun find(code: String): StrictDefectBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDefectBatch) {
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

    override fun list(): MutableList<StrictDefectBatch> {
        val out = mutableListOf<StrictDefectBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDefectBatchStore(): StrictDefectBatchStore {
    return MemoryStrictDefectBatchStore(mutableMapOf<String, StrictDefectBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDefectBatchStore(
    var inner: StrictDefectBatchStore,
    var cache: TtlCache<StrictDefectBatch>,
    var hits: Int,
    var misses: Int,
) : StrictDefectBatchStore {
    override fun find(code: String): StrictDefectBatch? {
        val hit = (cache.get(code) as StrictDefectBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDefectBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDefectBatch> = inner.list()
}

fun newCachedStrictDefectBatchStore(inner: StrictDefectBatchStore, ttl: Int, clock: Clock): StrictDefectBatchStore {
    return CachedStrictDefectBatchStore(
        inner = inner,
        cache = newTtlCache<StrictDefectBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict defect batches. Empty fields match all. */
data class StrictDefectBatchQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDefectBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDefectBatch>): MutableList<StrictDefectBatch> {
        val matched = filter(items) { candidate: StrictDefectBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDefectBatches(store: StrictDefectBatchStore, query: StrictDefectBatchQuery): MutableList<StrictDefectBatch> {
    return query.apply(store.list())
}
