package com.acme.transport.safety.violation.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted violation cycles by code. */
interface WeightedViolationCycleStore {
    fun find(code: String): WeightedViolationCycle?
    fun save(item: WeightedViolationCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedViolationCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedViolationCycleStore(
    var items: MutableMap<String, WeightedViolationCycle>,
    var order: MutableList<String>,
) : WeightedViolationCycleStore {
    override fun find(code: String): WeightedViolationCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedViolationCycle) {
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

    override fun list(): MutableList<WeightedViolationCycle> {
        val out = mutableListOf<WeightedViolationCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedViolationCycleStore(): WeightedViolationCycleStore {
    return MemoryWeightedViolationCycleStore(
        mutableMapOf<String, WeightedViolationCycle>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedViolationCycleStore(
    var inner: WeightedViolationCycleStore,
    var cache: TtlCache<WeightedViolationCycle>,
    var hits: Int,
    var misses: Int,
) : WeightedViolationCycleStore {
    override fun find(code: String): WeightedViolationCycle? {
        val hit = (cache.get(code) as WeightedViolationCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedViolationCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedViolationCycle> = inner.list()
}

fun newCachedWeightedViolationCycleStore(inner: WeightedViolationCycleStore, ttl: Int, clock: Clock): WeightedViolationCycleStore {
    return CachedWeightedViolationCycleStore(
        inner = inner,
        cache = newTtlCache<WeightedViolationCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted violation cycles. Empty fields match all. */
data class WeightedViolationCycleQuery(
    var depot: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedViolationCycle): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedViolationCycle>): MutableList<WeightedViolationCycle> {
        val matched = filter(items) { candidate: WeightedViolationCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedViolationCycles(store: WeightedViolationCycleStore, query: WeightedViolationCycleQuery): MutableList<WeightedViolationCycle> {
    return query.apply(store.list())
}
