package com.acme.transport.linehaul.laneplan.plansegment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched lane plan thresholds by code. */
interface BatchedLanePlanThresholdStore {
    fun find(code: String): BatchedLanePlanThreshold?
    fun save(item: BatchedLanePlanThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedLanePlanThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedLanePlanThresholdStore(
    var items: MutableMap<String, BatchedLanePlanThreshold>,
    var order: MutableList<String>,
) : BatchedLanePlanThresholdStore {
    override fun find(code: String): BatchedLanePlanThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedLanePlanThreshold) {
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

    override fun list(): MutableList<BatchedLanePlanThreshold> {
        val out = mutableListOf<BatchedLanePlanThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedLanePlanThresholdStore(): BatchedLanePlanThresholdStore {
    return MemoryBatchedLanePlanThresholdStore(
        mutableMapOf<String, BatchedLanePlanThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedLanePlanThresholdStore(
    var inner: BatchedLanePlanThresholdStore,
    var cache: TtlCache<BatchedLanePlanThreshold>,
    var hits: Int,
    var misses: Int,
) : BatchedLanePlanThresholdStore {
    override fun find(code: String): BatchedLanePlanThreshold? {
        val hit = (cache.get(code) as BatchedLanePlanThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedLanePlanThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedLanePlanThreshold> = inner.list()
}

fun newCachedBatchedLanePlanThresholdStore(inner: BatchedLanePlanThresholdStore, ttl: Int, clock: Clock): BatchedLanePlanThresholdStore {
    return CachedBatchedLanePlanThresholdStore(
        inner = inner,
        cache = newTtlCache<BatchedLanePlanThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched lane plan thresholds. Empty fields match all. */
data class BatchedLanePlanThresholdQuery(
    var category: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedLanePlanThreshold): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedLanePlanThreshold>): MutableList<BatchedLanePlanThreshold> {
        val matched = filter(items) { candidate: BatchedLanePlanThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedLanePlanThresholds(store: BatchedLanePlanThresholdStore, query: BatchedLanePlanThresholdQuery): MutableList<BatchedLanePlanThreshold> {
    return query.apply(store.list())
}
