package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps defect batches by code. */
interface DefectBatchStore {
    fun find(code: String): DefectBatch?
    fun save(item: DefectBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefectBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefectBatchStore(
    var items: MutableMap<String, DefectBatch>,
    var order: MutableList<String>,
) : DefectBatchStore {
    override fun find(code: String): DefectBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefectBatch) {
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

    override fun list(): MutableList<DefectBatch> {
        val out = mutableListOf<DefectBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefectBatchStore(): DefectBatchStore {
    return MemoryDefectBatchStore(mutableMapOf<String, DefectBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefectBatchStore(
    var inner: DefectBatchStore,
    var cache: TtlCache<DefectBatch>,
    var hits: Int,
    var misses: Int,
) : DefectBatchStore {
    override fun find(code: String): DefectBatch? {
        val hit = (cache.get(code) as DefectBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefectBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefectBatch> = inner.list()
}

fun newCachedDefectBatchStore(inner: DefectBatchStore, ttl: Int, clock: Clock): DefectBatchStore {
    return CachedDefectBatchStore(
        inner = inner,
        cache = newTtlCache<DefectBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects defect batches. Empty fields match all. */
data class DefectBatchQuery(
    var country: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefectBatch): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<DefectBatch>): MutableList<DefectBatch> {
        val matched = filter(items) { candidate: DefectBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefectBatches(store: DefectBatchStore, query: DefectBatchQuery): MutableList<DefectBatch> {
    return query.apply(store.list())
}
