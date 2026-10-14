package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn groups by code. */
interface FuelBurnGroupStore {
    fun find(code: String): FuelBurnGroup?
    fun save(item: FuelBurnGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnGroupStore(
    var items: MutableMap<String, FuelBurnGroup>,
    var order: MutableList<String>,
) : FuelBurnGroupStore {
    override fun find(code: String): FuelBurnGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnGroup) {
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

    override fun list(): MutableList<FuelBurnGroup> {
        val out = mutableListOf<FuelBurnGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnGroupStore(): FuelBurnGroupStore {
    return MemoryFuelBurnGroupStore(mutableMapOf<String, FuelBurnGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnGroupStore(
    var inner: FuelBurnGroupStore,
    var cache: TtlCache<FuelBurnGroup>,
    var hits: Int,
    var misses: Int,
) : FuelBurnGroupStore {
    override fun find(code: String): FuelBurnGroup? {
        val hit = (cache.get(code) as FuelBurnGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnGroup> = inner.list()
}

fun newCachedFuelBurnGroupStore(inner: FuelBurnGroupStore, ttl: Int, clock: Clock): FuelBurnGroupStore {
    return CachedFuelBurnGroupStore(
        inner = inner,
        cache = newTtlCache<FuelBurnGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn groups. Empty fields match all. */
data class FuelBurnGroupQuery(
    var lane: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnGroup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<FuelBurnGroup>): MutableList<FuelBurnGroup> {
        val matched = filter(items) { candidate: FuelBurnGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnGroups(store: FuelBurnGroupStore, query: FuelBurnGroupQuery): MutableList<FuelBurnGroup> {
    return query.apply(store.list())
}
