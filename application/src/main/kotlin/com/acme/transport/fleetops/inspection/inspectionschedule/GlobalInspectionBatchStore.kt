package com.acme.transport.fleetops.inspection.inspectionschedule

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps global inspection batches by code. */
interface GlobalInspectionBatchStore {
    fun find(code: String): GlobalInspectionBatch?
    fun save(item: GlobalInspectionBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalInspectionBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalInspectionBatchStore(
    var items: MutableMap<String, GlobalInspectionBatch>,
    var order: MutableList<String>,
) : GlobalInspectionBatchStore {
    override fun find(code: String): GlobalInspectionBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalInspectionBatch) {
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

    override fun list(): MutableList<GlobalInspectionBatch> {
        val out = mutableListOf<GlobalInspectionBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalInspectionBatchStore(): GlobalInspectionBatchStore {
    return MemoryGlobalInspectionBatchStore(
        mutableMapOf<String, GlobalInspectionBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalInspectionBatchStore(
    var inner: GlobalInspectionBatchStore,
    var cache: TtlCache<GlobalInspectionBatch>,
    var hits: Int,
    var misses: Int,
) : GlobalInspectionBatchStore {
    override fun find(code: String): GlobalInspectionBatch? {
        val hit = (cache.get(code) as GlobalInspectionBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalInspectionBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalInspectionBatch> = inner.list()
}

fun newCachedGlobalInspectionBatchStore(inner: GlobalInspectionBatchStore, ttl: Int, clock: Clock): GlobalInspectionBatchStore {
    return CachedGlobalInspectionBatchStore(
        inner = inner,
        cache = newTtlCache<GlobalInspectionBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global inspection batches. Empty fields match all. */
data class GlobalInspectionBatchQuery(
    var label: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalInspectionBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalInspectionBatch>): MutableList<GlobalInspectionBatch> {
        val matched = filter(items) { candidate: GlobalInspectionBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalInspectionBatches(store: GlobalInspectionBatchStore, query: GlobalInspectionBatchQuery): MutableList<GlobalInspectionBatch> {
    return query.apply(store.list())
}
