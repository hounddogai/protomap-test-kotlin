package com.acme.transport.linehaul.capacity.summary

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered capacity batches by code. */
interface TieredCapacityBatchStore {
    fun find(code: String): TieredCapacityBatch?
    fun save(item: TieredCapacityBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCapacityBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCapacityBatchStore(
    var items: MutableMap<String, TieredCapacityBatch>,
    var order: MutableList<String>,
) : TieredCapacityBatchStore {
    override fun find(code: String): TieredCapacityBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCapacityBatch) {
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

    override fun list(): MutableList<TieredCapacityBatch> {
        val out = mutableListOf<TieredCapacityBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCapacityBatchStore(): TieredCapacityBatchStore {
    return MemoryTieredCapacityBatchStore(mutableMapOf<String, TieredCapacityBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCapacityBatchStore(
    var inner: TieredCapacityBatchStore,
    var cache: TtlCache<TieredCapacityBatch>,
    var hits: Int,
    var misses: Int,
) : TieredCapacityBatchStore {
    override fun find(code: String): TieredCapacityBatch? {
        val hit = (cache.get(code) as TieredCapacityBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCapacityBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCapacityBatch> = inner.list()
}

fun newCachedTieredCapacityBatchStore(inner: TieredCapacityBatchStore, ttl: Int, clock: Clock): TieredCapacityBatchStore {
    return CachedTieredCapacityBatchStore(
        inner = inner,
        cache = newTtlCache<TieredCapacityBatch>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered capacity batches. Empty fields match all. */
data class TieredCapacityBatchQuery(
    var segment: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCapacityBatch): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCapacityBatch>): MutableList<TieredCapacityBatch> {
        val matched = filter(items) { candidate: TieredCapacityBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCapacityBatches(store: TieredCapacityBatchStore, query: TieredCapacityBatchQuery): MutableList<TieredCapacityBatch> {
    return query.apply(store.list())
}
