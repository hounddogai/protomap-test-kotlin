package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted fuel burn adjustments by code. */
interface WeightedFuelBurnAdjustmentStore {
    fun find(code: String): WeightedFuelBurnAdjustment?
    fun save(item: WeightedFuelBurnAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedFuelBurnAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedFuelBurnAdjustmentStore(
    var items: MutableMap<String, WeightedFuelBurnAdjustment>,
    var order: MutableList<String>,
) : WeightedFuelBurnAdjustmentStore {
    override fun find(code: String): WeightedFuelBurnAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedFuelBurnAdjustment) {
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

    override fun list(): MutableList<WeightedFuelBurnAdjustment> {
        val out = mutableListOf<WeightedFuelBurnAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedFuelBurnAdjustmentStore(): WeightedFuelBurnAdjustmentStore {
    return MemoryWeightedFuelBurnAdjustmentStore(
        mutableMapOf<String, WeightedFuelBurnAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedFuelBurnAdjustmentStore(
    var inner: WeightedFuelBurnAdjustmentStore,
    var cache: TtlCache<WeightedFuelBurnAdjustment>,
    var hits: Int,
    var misses: Int,
) : WeightedFuelBurnAdjustmentStore {
    override fun find(code: String): WeightedFuelBurnAdjustment? {
        val hit = (cache.get(code) as WeightedFuelBurnAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedFuelBurnAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedFuelBurnAdjustment> = inner.list()
}

fun newCachedWeightedFuelBurnAdjustmentStore(inner: WeightedFuelBurnAdjustmentStore, ttl: Int, clock: Clock): WeightedFuelBurnAdjustmentStore {
    return CachedWeightedFuelBurnAdjustmentStore(
        inner = inner,
        cache = newTtlCache<WeightedFuelBurnAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted fuel burn adjustments. Empty fields match all. */
data class WeightedFuelBurnAdjustmentQuery(
    var channel: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedFuelBurnAdjustment): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<WeightedFuelBurnAdjustment>): MutableList<WeightedFuelBurnAdjustment> {
        val matched = filter(items) { candidate: WeightedFuelBurnAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedFuelBurnAdjustments(store: WeightedFuelBurnAdjustmentStore, query: WeightedFuelBurnAdjustmentQuery): MutableList<WeightedFuelBurnAdjustment> {
    return query.apply(store.list())
}
