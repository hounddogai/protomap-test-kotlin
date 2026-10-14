package com.acme.transport.emissions.fuelburn.ledger

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted fuel burn mappings by code. */
interface WeightedFuelBurnMappingStore {
    fun find(code: String): WeightedFuelBurnMapping?
    fun save(item: WeightedFuelBurnMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedFuelBurnMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedFuelBurnMappingStore(
    var items: MutableMap<String, WeightedFuelBurnMapping>,
    var order: MutableList<String>,
) : WeightedFuelBurnMappingStore {
    override fun find(code: String): WeightedFuelBurnMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedFuelBurnMapping) {
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

    override fun list(): MutableList<WeightedFuelBurnMapping> {
        val out = mutableListOf<WeightedFuelBurnMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedFuelBurnMappingStore(): WeightedFuelBurnMappingStore {
    return MemoryWeightedFuelBurnMappingStore(
        mutableMapOf<String, WeightedFuelBurnMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedFuelBurnMappingStore(
    var inner: WeightedFuelBurnMappingStore,
    var cache: TtlCache<WeightedFuelBurnMapping>,
    var hits: Int,
    var misses: Int,
) : WeightedFuelBurnMappingStore {
    override fun find(code: String): WeightedFuelBurnMapping? {
        val hit = (cache.get(code) as WeightedFuelBurnMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedFuelBurnMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedFuelBurnMapping> = inner.list()
}

fun newCachedWeightedFuelBurnMappingStore(inner: WeightedFuelBurnMappingStore, ttl: Int, clock: Clock): WeightedFuelBurnMappingStore {
    return CachedWeightedFuelBurnMappingStore(
        inner = inner,
        cache = newTtlCache<WeightedFuelBurnMapping>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted fuel burn mappings. Empty fields match all. */
data class WeightedFuelBurnMappingQuery(
    var lane: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedFuelBurnMapping): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedFuelBurnMapping>): MutableList<WeightedFuelBurnMapping> {
        val matched = filter(items) { candidate: WeightedFuelBurnMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedFuelBurnMappings(store: WeightedFuelBurnMappingStore, query: WeightedFuelBurnMappingQuery): MutableList<WeightedFuelBurnMapping> {
    return query.apply(store.list())
}
