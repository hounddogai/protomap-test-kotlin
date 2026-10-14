package com.acme.transport.emissions.emissionfactor.claim

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted emission factor thresholds by code. */
interface WeightedEmissionFactorThresholdStore {
    fun find(code: String): WeightedEmissionFactorThreshold?
    fun save(item: WeightedEmissionFactorThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedEmissionFactorThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedEmissionFactorThresholdStore(
    var items: MutableMap<String, WeightedEmissionFactorThreshold>,
    var order: MutableList<String>,
) : WeightedEmissionFactorThresholdStore {
    override fun find(code: String): WeightedEmissionFactorThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedEmissionFactorThreshold) {
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

    override fun list(): MutableList<WeightedEmissionFactorThreshold> {
        val out = mutableListOf<WeightedEmissionFactorThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedEmissionFactorThresholdStore(): WeightedEmissionFactorThresholdStore {
    return MemoryWeightedEmissionFactorThresholdStore(
        mutableMapOf<String, WeightedEmissionFactorThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedEmissionFactorThresholdStore(
    var inner: WeightedEmissionFactorThresholdStore,
    var cache: TtlCache<WeightedEmissionFactorThreshold>,
    var hits: Int,
    var misses: Int,
) : WeightedEmissionFactorThresholdStore {
    override fun find(code: String): WeightedEmissionFactorThreshold? {
        val hit = (cache.get(code) as WeightedEmissionFactorThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedEmissionFactorThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedEmissionFactorThreshold> = inner.list()
}

fun newCachedWeightedEmissionFactorThresholdStore(inner: WeightedEmissionFactorThresholdStore, ttl: Int, clock: Clock): WeightedEmissionFactorThresholdStore {
    return CachedWeightedEmissionFactorThresholdStore(
        inner = inner,
        cache = newTtlCache<WeightedEmissionFactorThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted emission factor thresholds. Empty fields match all. */
data class WeightedEmissionFactorThresholdQuery(
    var category: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedEmissionFactorThreshold): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedEmissionFactorThreshold>): MutableList<WeightedEmissionFactorThreshold> {
        val matched = filter(items) { candidate: WeightedEmissionFactorThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedEmissionFactorThresholds(store: WeightedEmissionFactorThresholdStore, query: WeightedEmissionFactorThresholdQuery): MutableList<WeightedEmissionFactorThreshold> {
    return query.apply(store.list())
}
