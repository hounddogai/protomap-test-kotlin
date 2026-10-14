package com.acme.transport.network.node.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted node thresholds by code. */
interface WeightedNodeThresholdStore {
    fun find(code: String): WeightedNodeThreshold?
    fun save(item: WeightedNodeThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedNodeThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedNodeThresholdStore(
    var items: MutableMap<String, WeightedNodeThreshold>,
    var order: MutableList<String>,
) : WeightedNodeThresholdStore {
    override fun find(code: String): WeightedNodeThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedNodeThreshold) {
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

    override fun list(): MutableList<WeightedNodeThreshold> {
        val out = mutableListOf<WeightedNodeThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedNodeThresholdStore(): WeightedNodeThresholdStore {
    return MemoryWeightedNodeThresholdStore(
        mutableMapOf<String, WeightedNodeThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedNodeThresholdStore(
    var inner: WeightedNodeThresholdStore,
    var cache: TtlCache<WeightedNodeThreshold>,
    var hits: Int,
    var misses: Int,
) : WeightedNodeThresholdStore {
    override fun find(code: String): WeightedNodeThreshold? {
        val hit = (cache.get(code) as WeightedNodeThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedNodeThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedNodeThreshold> = inner.list()
}

fun newCachedWeightedNodeThresholdStore(inner: WeightedNodeThresholdStore, ttl: Int, clock: Clock): WeightedNodeThresholdStore {
    return CachedWeightedNodeThresholdStore(
        inner = inner,
        cache = newTtlCache<WeightedNodeThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted node thresholds. Empty fields match all. */
data class WeightedNodeThresholdQuery(
    var unit: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedNodeThreshold): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedNodeThreshold>): MutableList<WeightedNodeThreshold> {
        val matched = filter(items) { candidate: WeightedNodeThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedNodeThresholds(store: WeightedNodeThresholdStore, query: WeightedNodeThresholdQuery): MutableList<WeightedNodeThreshold> {
    return query.apply(store.list())
}
