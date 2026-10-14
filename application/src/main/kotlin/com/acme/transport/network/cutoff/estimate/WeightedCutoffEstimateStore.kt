package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted cutoff estimates by code. */
interface WeightedCutoffEstimateStore {
    fun find(code: String): WeightedCutoffEstimate?
    fun save(item: WeightedCutoffEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCutoffEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCutoffEstimateStore(
    var items: MutableMap<String, WeightedCutoffEstimate>,
    var order: MutableList<String>,
) : WeightedCutoffEstimateStore {
    override fun find(code: String): WeightedCutoffEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCutoffEstimate) {
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

    override fun list(): MutableList<WeightedCutoffEstimate> {
        val out = mutableListOf<WeightedCutoffEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCutoffEstimateStore(): WeightedCutoffEstimateStore {
    return MemoryWeightedCutoffEstimateStore(
        mutableMapOf<String, WeightedCutoffEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCutoffEstimateStore(
    var inner: WeightedCutoffEstimateStore,
    var cache: TtlCache<WeightedCutoffEstimate>,
    var hits: Int,
    var misses: Int,
) : WeightedCutoffEstimateStore {
    override fun find(code: String): WeightedCutoffEstimate? {
        val hit = (cache.get(code) as WeightedCutoffEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCutoffEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCutoffEstimate> = inner.list()
}

fun newCachedWeightedCutoffEstimateStore(inner: WeightedCutoffEstimateStore, ttl: Int, clock: Clock): WeightedCutoffEstimateStore {
    return CachedWeightedCutoffEstimateStore(
        inner = inner,
        cache = newTtlCache<WeightedCutoffEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted cutoff estimates. Empty fields match all. */
data class WeightedCutoffEstimateQuery(
    var label: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCutoffEstimate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<WeightedCutoffEstimate>): MutableList<WeightedCutoffEstimate> {
        val matched = filter(items) { candidate: WeightedCutoffEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCutoffEstimates(store: WeightedCutoffEstimateStore, query: WeightedCutoffEstimateQuery): MutableList<WeightedCutoffEstimate> {
    return query.apply(store.list())
}
