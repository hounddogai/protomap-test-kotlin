package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted trailer batches by code. */
interface WeightedTrailerBatchStore {
    fun find(code: String): WeightedTrailerBatch?
    fun save(item: WeightedTrailerBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTrailerBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTrailerBatchStore(
    var items: MutableMap<String, WeightedTrailerBatch>,
    var order: MutableList<String>,
) : WeightedTrailerBatchStore {
    override fun find(code: String): WeightedTrailerBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTrailerBatch) {
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

    override fun list(): MutableList<WeightedTrailerBatch> {
        val out = mutableListOf<WeightedTrailerBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTrailerBatchStore(): WeightedTrailerBatchStore {
    return MemoryWeightedTrailerBatchStore(
        mutableMapOf<String, WeightedTrailerBatch>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTrailerBatchStore(
    var inner: WeightedTrailerBatchStore,
    var cache: TtlCache<WeightedTrailerBatch>,
    var hits: Int,
    var misses: Int,
) : WeightedTrailerBatchStore {
    override fun find(code: String): WeightedTrailerBatch? {
        val hit = (cache.get(code) as WeightedTrailerBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTrailerBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTrailerBatch> = inner.list()
}

fun newCachedWeightedTrailerBatchStore(inner: WeightedTrailerBatchStore, ttl: Int, clock: Clock): WeightedTrailerBatchStore {
    return CachedWeightedTrailerBatchStore(
        inner = inner,
        cache = newTtlCache<WeightedTrailerBatch>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted trailer batches. Empty fields match all. */
data class WeightedTrailerBatchQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTrailerBatch): Boolean {
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

    fun apply(items: MutableList<WeightedTrailerBatch>): MutableList<WeightedTrailerBatch> {
        val matched = filter(items) { candidate: WeightedTrailerBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTrailerBatches(store: WeightedTrailerBatchStore, query: WeightedTrailerBatchQuery): MutableList<WeightedTrailerBatch> {
    return query.apply(store.list())
}
