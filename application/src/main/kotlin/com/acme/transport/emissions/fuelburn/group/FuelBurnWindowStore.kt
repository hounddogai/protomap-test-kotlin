package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn windows by code. */
interface FuelBurnWindowStore {
    fun find(code: String): FuelBurnWindow?
    fun save(item: FuelBurnWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnWindowStore(
    var items: MutableMap<String, FuelBurnWindow>,
    var order: MutableList<String>,
) : FuelBurnWindowStore {
    override fun find(code: String): FuelBurnWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnWindow) {
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

    override fun list(): MutableList<FuelBurnWindow> {
        val out = mutableListOf<FuelBurnWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnWindowStore(): FuelBurnWindowStore {
    return MemoryFuelBurnWindowStore(mutableMapOf<String, FuelBurnWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnWindowStore(
    var inner: FuelBurnWindowStore,
    var cache: TtlCache<FuelBurnWindow>,
    var hits: Int,
    var misses: Int,
) : FuelBurnWindowStore {
    override fun find(code: String): FuelBurnWindow? {
        val hit = (cache.get(code) as FuelBurnWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnWindow> = inner.list()
}

fun newCachedFuelBurnWindowStore(inner: FuelBurnWindowStore, ttl: Int, clock: Clock): FuelBurnWindowStore {
    return CachedFuelBurnWindowStore(
        inner = inner,
        cache = newTtlCache<FuelBurnWindow>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn windows. Empty fields match all. */
data class FuelBurnWindowQuery(
    var source: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnWindow): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnWindow>): MutableList<FuelBurnWindow> {
        val matched = filter(items) { candidate: FuelBurnWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnWindows(store: FuelBurnWindowStore, query: FuelBurnWindowQuery): MutableList<FuelBurnWindow> {
    return query.apply(store.list())
}
