package com.acme.transport.fleetops.inspection.batch

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps inspection batches by code. */
interface InspectionBatchStore {
    fun find(code: String): InspectionBatch?
    fun save(item: InspectionBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<InspectionBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryInspectionBatchStore(
    var items: MutableMap<String, InspectionBatch>,
    var order: MutableList<String>,
) : InspectionBatchStore {
    override fun find(code: String): InspectionBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: InspectionBatch) {
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

    override fun list(): MutableList<InspectionBatch> {
        val out = mutableListOf<InspectionBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryInspectionBatchStore(): InspectionBatchStore {
    return MemoryInspectionBatchStore(mutableMapOf<String, InspectionBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedInspectionBatchStore(
    var inner: InspectionBatchStore,
    var cache: TtlCache<InspectionBatch>,
    var hits: Int,
    var misses: Int,
) : InspectionBatchStore {
    override fun find(code: String): InspectionBatch? {
        val hit = (cache.get(code) as InspectionBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: InspectionBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<InspectionBatch> = inner.list()
}

fun newCachedInspectionBatchStore(inner: InspectionBatchStore, ttl: Int, clock: Clock): InspectionBatchStore {
    return CachedInspectionBatchStore(
        inner = inner,
        cache = newTtlCache<InspectionBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects inspection batches. Empty fields match all. */
data class InspectionBatchQuery(
    var name: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: InspectionBatch): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<InspectionBatch>): MutableList<InspectionBatch> {
        val matched = filter(items) { candidate: InspectionBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findInspectionBatches(store: InspectionBatchStore, query: InspectionBatchQuery): MutableList<InspectionBatch> {
    return query.apply(store.list())
}
