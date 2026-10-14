package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted fuel burn windows by code. */
interface WeightedFuelBurnWindowStore {
    fun find(code: String): WeightedFuelBurnWindow?
    fun save(item: WeightedFuelBurnWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedFuelBurnWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedFuelBurnWindowStore(
    var items: MutableMap<String, WeightedFuelBurnWindow>,
    var order: MutableList<String>,
) : WeightedFuelBurnWindowStore {
    override fun find(code: String): WeightedFuelBurnWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedFuelBurnWindow) {
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

    override fun list(): MutableList<WeightedFuelBurnWindow> {
        val out = mutableListOf<WeightedFuelBurnWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedFuelBurnWindowStore(): WeightedFuelBurnWindowStore {
    return MemoryWeightedFuelBurnWindowStore(
        mutableMapOf<String, WeightedFuelBurnWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedFuelBurnWindowStore(
    var inner: WeightedFuelBurnWindowStore,
    var cache: TtlCache<WeightedFuelBurnWindow>,
    var hits: Int,
    var misses: Int,
) : WeightedFuelBurnWindowStore {
    override fun find(code: String): WeightedFuelBurnWindow? {
        val hit = (cache.get(code) as WeightedFuelBurnWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedFuelBurnWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedFuelBurnWindow> = inner.list()
}

fun newCachedWeightedFuelBurnWindowStore(inner: WeightedFuelBurnWindowStore, ttl: Int, clock: Clock): WeightedFuelBurnWindowStore {
    return CachedWeightedFuelBurnWindowStore(
        inner = inner,
        cache = newTtlCache<WeightedFuelBurnWindow>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted fuel burn windows. Empty fields match all. */
data class WeightedFuelBurnWindowQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedFuelBurnWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedFuelBurnWindow>): MutableList<WeightedFuelBurnWindow> {
        val matched = filter(items) { candidate: WeightedFuelBurnWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedFuelBurnWindows(store: WeightedFuelBurnWindowStore, query: WeightedFuelBurnWindowQuery): MutableList<WeightedFuelBurnWindow> {
    return query.apply(store.list())
}
