package com.acme.transport.customs.inspection.audit

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending inspection batches by code. */
interface PendingInspectionBatchStore {
    fun find(code: String): PendingInspectionBatch?
    fun save(item: PendingInspectionBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingInspectionBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingInspectionBatchStore(
    var items: MutableMap<String, PendingInspectionBatch>,
    var order: MutableList<String>,
) : PendingInspectionBatchStore {
    override fun find(code: String): PendingInspectionBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingInspectionBatch) {
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

    override fun list(): MutableList<PendingInspectionBatch> {
        val out = mutableListOf<PendingInspectionBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingInspectionBatchStore(): PendingInspectionBatchStore {
    return MemoryPendingInspectionBatchStore(
        mutableMapOf<String, PendingInspectionBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingInspectionBatchStore(
    var inner: PendingInspectionBatchStore,
    var cache: TtlCache<PendingInspectionBatch>,
    var hits: Int,
    var misses: Int,
) : PendingInspectionBatchStore {
    override fun find(code: String): PendingInspectionBatch? {
        val hit = (cache.get(code) as PendingInspectionBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingInspectionBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingInspectionBatch> = inner.list()
}

fun newCachedPendingInspectionBatchStore(inner: PendingInspectionBatchStore, ttl: Int, clock: Clock): PendingInspectionBatchStore {
    return CachedPendingInspectionBatchStore(
        inner = inner,
        cache = newTtlCache<PendingInspectionBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending inspection batches. Empty fields match all. */
data class PendingInspectionBatchQuery(
    var region: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingInspectionBatch): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingInspectionBatch>): MutableList<PendingInspectionBatch> {
        val matched = filter(items) { candidate: PendingInspectionBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingInspectionBatches(store: PendingInspectionBatchStore, query: PendingInspectionBatchQuery): MutableList<PendingInspectionBatch> {
    return query.apply(store.list())
}
