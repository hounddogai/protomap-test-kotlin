package com.acme.transport.safety.nearmiss.missestimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps weighted near miss estimates by code. */
interface WeightedNearMissEstimateStore {
    fun find(code: String): WeightedNearMissEstimate?
    fun save(item: WeightedNearMissEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedNearMissEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedNearMissEstimateStore(
    var items: MutableMap<String, WeightedNearMissEstimate>,
    var order: MutableList<String>,
) : WeightedNearMissEstimateStore {
    override fun find(code: String): WeightedNearMissEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedNearMissEstimate) {
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

    override fun list(): MutableList<WeightedNearMissEstimate> {
        val out = mutableListOf<WeightedNearMissEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedNearMissEstimateStore(): WeightedNearMissEstimateStore {
    return MemoryWeightedNearMissEstimateStore(
        mutableMapOf<String, WeightedNearMissEstimate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedNearMissEstimateStore(
    var inner: WeightedNearMissEstimateStore,
    var cache: TtlCache<WeightedNearMissEstimate>,
    var hits: Int,
    var misses: Int,
) : WeightedNearMissEstimateStore {
    override fun find(code: String): WeightedNearMissEstimate? {
        val hit = (cache.get(code) as WeightedNearMissEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedNearMissEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedNearMissEstimate> = inner.list()
}

fun newCachedWeightedNearMissEstimateStore(inner: WeightedNearMissEstimateStore, ttl: Int, clock: Clock): WeightedNearMissEstimateStore {
    return CachedWeightedNearMissEstimateStore(
        inner = inner,
        cache = newTtlCache<WeightedNearMissEstimate>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted near miss estimates. Empty fields match all. */
data class WeightedNearMissEstimateQuery(
    var locale: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedNearMissEstimate): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedNearMissEstimate>): MutableList<WeightedNearMissEstimate> {
        val matched = filter(items) { candidate: WeightedNearMissEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedNearMissEstimates(store: WeightedNearMissEstimateStore, query: WeightedNearMissEstimateQuery): MutableList<WeightedNearMissEstimate> {
    return query.apply(store.list())
}
