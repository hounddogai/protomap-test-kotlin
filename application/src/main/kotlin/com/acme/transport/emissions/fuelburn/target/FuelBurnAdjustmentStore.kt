package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn adjustments by code. */
interface FuelBurnAdjustmentStore {
    fun find(code: String): FuelBurnAdjustment?
    fun save(item: FuelBurnAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnAdjustmentStore(
    var items: MutableMap<String, FuelBurnAdjustment>,
    var order: MutableList<String>,
) : FuelBurnAdjustmentStore {
    override fun find(code: String): FuelBurnAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnAdjustment) {
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

    override fun list(): MutableList<FuelBurnAdjustment> {
        val out = mutableListOf<FuelBurnAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnAdjustmentStore(): FuelBurnAdjustmentStore {
    return MemoryFuelBurnAdjustmentStore(mutableMapOf<String, FuelBurnAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnAdjustmentStore(
    var inner: FuelBurnAdjustmentStore,
    var cache: TtlCache<FuelBurnAdjustment>,
    var hits: Int,
    var misses: Int,
) : FuelBurnAdjustmentStore {
    override fun find(code: String): FuelBurnAdjustment? {
        val hit = (cache.get(code) as FuelBurnAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnAdjustment> = inner.list()
}

fun newCachedFuelBurnAdjustmentStore(inner: FuelBurnAdjustmentStore, ttl: Int, clock: Clock): FuelBurnAdjustmentStore {
    return CachedFuelBurnAdjustmentStore(
        inner = inner,
        cache = newTtlCache<FuelBurnAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn adjustments. Empty fields match all. */
data class FuelBurnAdjustmentQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnAdjustment): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnAdjustment>): MutableList<FuelBurnAdjustment> {
        val matched = filter(items) { candidate: FuelBurnAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnAdjustments(store: FuelBurnAdjustmentStore, query: FuelBurnAdjustmentQuery): MutableList<FuelBurnAdjustment> {
    return query.apply(store.list())
}
