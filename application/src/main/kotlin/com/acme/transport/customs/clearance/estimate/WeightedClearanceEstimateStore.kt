package com.acme.transport.customs.clearance.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted clearance estimates by code. */
interface WeightedClearanceEstimateStore {
    fun find(code: String): WeightedClearanceEstimate?
    fun save(item: WeightedClearanceEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedClearanceEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedClearanceEstimateStore(
    var items: MutableMap<String, WeightedClearanceEstimate>,
    var order: MutableList<String>,
) : WeightedClearanceEstimateStore {
    override fun find(code: String): WeightedClearanceEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedClearanceEstimate) {
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

    override fun list(): MutableList<WeightedClearanceEstimate> {
        val out = mutableListOf<WeightedClearanceEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedClearanceEstimateStore(): WeightedClearanceEstimateStore {
    return MemoryWeightedClearanceEstimateStore(
        mutableMapOf<String, WeightedClearanceEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedClearanceEstimateStore(
    var inner: WeightedClearanceEstimateStore,
    var cache: TtlCache<WeightedClearanceEstimate>,
    var hits: Int,
    var misses: Int,
) : WeightedClearanceEstimateStore {
    override fun find(code: String): WeightedClearanceEstimate? {
        val hit = (cache.get(code) as WeightedClearanceEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedClearanceEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedClearanceEstimate> = inner.list()
}

fun newCachedWeightedClearanceEstimateStore(inner: WeightedClearanceEstimateStore, ttl: Int, clock: Clock): WeightedClearanceEstimateStore {
    return CachedWeightedClearanceEstimateStore(
        inner = inner,
        cache = newTtlCache<WeightedClearanceEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted clearance estimates. Empty fields match all. */
data class WeightedClearanceEstimateQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedClearanceEstimate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedClearanceEstimate>): MutableList<WeightedClearanceEstimate> {
        val matched = filter(items) { candidate: WeightedClearanceEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedClearanceEstimates(store: WeightedClearanceEstimateStore, query: WeightedClearanceEstimateQuery): MutableList<WeightedClearanceEstimate> {
    return query.apply(store.list())
}
