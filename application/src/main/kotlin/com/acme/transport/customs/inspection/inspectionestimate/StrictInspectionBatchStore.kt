package com.acme.transport.customs.inspection.inspectionestimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict inspection batches by code. */
interface StrictInspectionBatchStore {
    fun find(code: String): StrictInspectionBatch?
    fun save(item: StrictInspectionBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictInspectionBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictInspectionBatchStore(
    var items: MutableMap<String, StrictInspectionBatch>,
    var order: MutableList<String>,
) : StrictInspectionBatchStore {
    override fun find(code: String): StrictInspectionBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictInspectionBatch) {
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

    override fun list(): MutableList<StrictInspectionBatch> {
        val out = mutableListOf<StrictInspectionBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictInspectionBatchStore(): StrictInspectionBatchStore {
    return MemoryStrictInspectionBatchStore(
        mutableMapOf<String, StrictInspectionBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictInspectionBatchStore(
    var inner: StrictInspectionBatchStore,
    var cache: TtlCache<StrictInspectionBatch>,
    var hits: Int,
    var misses: Int,
) : StrictInspectionBatchStore {
    override fun find(code: String): StrictInspectionBatch? {
        val hit = (cache.get(code) as StrictInspectionBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictInspectionBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictInspectionBatch> = inner.list()
}

fun newCachedStrictInspectionBatchStore(inner: StrictInspectionBatchStore, ttl: Int, clock: Clock): StrictInspectionBatchStore {
    return CachedStrictInspectionBatchStore(
        inner = inner,
        cache = newTtlCache<StrictInspectionBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict inspection batches. Empty fields match all. */
data class StrictInspectionBatchQuery(
    var label: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictInspectionBatch): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictInspectionBatch>): MutableList<StrictInspectionBatch> {
        val matched = filter(items) { candidate: StrictInspectionBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictInspectionBatches(store: StrictInspectionBatchStore, query: StrictInspectionBatchQuery): MutableList<StrictInspectionBatch> {
    return query.apply(store.list())
}
