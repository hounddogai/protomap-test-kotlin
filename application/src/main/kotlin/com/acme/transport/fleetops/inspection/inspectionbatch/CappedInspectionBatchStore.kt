package com.acme.transport.fleetops.inspection.inspectionbatch

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps capped inspection batches by code. */
interface CappedInspectionBatchStore {
    fun find(code: String): CappedInspectionBatch?
    fun save(item: CappedInspectionBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedInspectionBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedInspectionBatchStore(
    var items: MutableMap<String, CappedInspectionBatch>,
    var order: MutableList<String>,
) : CappedInspectionBatchStore {
    override fun find(code: String): CappedInspectionBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedInspectionBatch) {
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

    override fun list(): MutableList<CappedInspectionBatch> {
        val out = mutableListOf<CappedInspectionBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedInspectionBatchStore(): CappedInspectionBatchStore {
    return MemoryCappedInspectionBatchStore(
        mutableMapOf<String, CappedInspectionBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedInspectionBatchStore(
    var inner: CappedInspectionBatchStore,
    var cache: TtlCache<CappedInspectionBatch>,
    var hits: Int,
    var misses: Int,
) : CappedInspectionBatchStore {
    override fun find(code: String): CappedInspectionBatch? {
        val hit = (cache.get(code) as CappedInspectionBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedInspectionBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedInspectionBatch> = inner.list()
}

fun newCachedCappedInspectionBatchStore(inner: CappedInspectionBatchStore, ttl: Int, clock: Clock): CappedInspectionBatchStore {
    return CachedCappedInspectionBatchStore(
        inner = inner,
        cache = newTtlCache<CappedInspectionBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped inspection batches. Empty fields match all. */
data class CappedInspectionBatchQuery(
    var label: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedInspectionBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedInspectionBatch>): MutableList<CappedInspectionBatch> {
        val matched = filter(items) { candidate: CappedInspectionBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedInspectionBatches(store: CappedInspectionBatchStore, query: CappedInspectionBatchQuery): MutableList<CappedInspectionBatch> {
    return query.apply(store.list())
}
