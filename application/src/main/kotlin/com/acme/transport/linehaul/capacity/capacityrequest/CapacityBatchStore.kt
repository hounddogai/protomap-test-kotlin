package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps capacity batches by code. */
interface CapacityBatchStore {
    fun find(code: String): CapacityBatch?
    fun save(item: CapacityBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<CapacityBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCapacityBatchStore(
    var items: MutableMap<String, CapacityBatch>,
    var order: MutableList<String>,
) : CapacityBatchStore {
    override fun find(code: String): CapacityBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CapacityBatch) {
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

    override fun list(): MutableList<CapacityBatch> {
        val out = mutableListOf<CapacityBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCapacityBatchStore(): CapacityBatchStore {
    return MemoryCapacityBatchStore(mutableMapOf<String, CapacityBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCapacityBatchStore(
    var inner: CapacityBatchStore,
    var cache: TtlCache<CapacityBatch>,
    var hits: Int,
    var misses: Int,
) : CapacityBatchStore {
    override fun find(code: String): CapacityBatch? {
        val hit = (cache.get(code) as CapacityBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CapacityBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CapacityBatch> = inner.list()
}

fun newCachedCapacityBatchStore(inner: CapacityBatchStore, ttl: Int, clock: Clock): CapacityBatchStore {
    return CachedCapacityBatchStore(
        inner = inner,
        cache = newTtlCache<CapacityBatch>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capacity batches. Empty fields match all. */
data class CapacityBatchQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CapacityBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CapacityBatch>): MutableList<CapacityBatch> {
        val matched = filter(items) { candidate: CapacityBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCapacityBatches(store: CapacityBatchStore, query: CapacityBatchQuery): MutableList<CapacityBatch> {
    return query.apply(store.list())
}
