package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn overrides by code. */
interface FuelBurnOverrideStore {
    fun find(code: String): FuelBurnOverride?
    fun save(item: FuelBurnOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnOverrideStore(
    var items: MutableMap<String, FuelBurnOverride>,
    var order: MutableList<String>,
) : FuelBurnOverrideStore {
    override fun find(code: String): FuelBurnOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnOverride) {
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

    override fun list(): MutableList<FuelBurnOverride> {
        val out = mutableListOf<FuelBurnOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnOverrideStore(): FuelBurnOverrideStore {
    return MemoryFuelBurnOverrideStore(mutableMapOf<String, FuelBurnOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnOverrideStore(
    var inner: FuelBurnOverrideStore,
    var cache: TtlCache<FuelBurnOverride>,
    var hits: Int,
    var misses: Int,
) : FuelBurnOverrideStore {
    override fun find(code: String): FuelBurnOverride? {
        val hit = (cache.get(code) as FuelBurnOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnOverride> = inner.list()
}

fun newCachedFuelBurnOverrideStore(inner: FuelBurnOverrideStore, ttl: Int, clock: Clock): FuelBurnOverrideStore {
    return CachedFuelBurnOverrideStore(
        inner = inner,
        cache = newTtlCache<FuelBurnOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn overrides. Empty fields match all. */
data class FuelBurnOverrideQuery(
    var name: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnOverride>): MutableList<FuelBurnOverride> {
        val matched = filter(items) { candidate: FuelBurnOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnOverrides(store: FuelBurnOverrideStore, query: FuelBurnOverrideQuery): MutableList<FuelBurnOverride> {
    return query.apply(store.list())
}
