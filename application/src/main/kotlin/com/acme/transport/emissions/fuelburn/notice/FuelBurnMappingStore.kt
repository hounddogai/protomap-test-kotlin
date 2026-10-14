package com.acme.transport.emissions.fuelburn.notice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn mappings by code. */
interface FuelBurnMappingStore {
    fun find(code: String): FuelBurnMapping?
    fun save(item: FuelBurnMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnMappingStore(
    var items: MutableMap<String, FuelBurnMapping>,
    var order: MutableList<String>,
) : FuelBurnMappingStore {
    override fun find(code: String): FuelBurnMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnMapping) {
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

    override fun list(): MutableList<FuelBurnMapping> {
        val out = mutableListOf<FuelBurnMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnMappingStore(): FuelBurnMappingStore {
    return MemoryFuelBurnMappingStore(mutableMapOf<String, FuelBurnMapping>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnMappingStore(
    var inner: FuelBurnMappingStore,
    var cache: TtlCache<FuelBurnMapping>,
    var hits: Int,
    var misses: Int,
) : FuelBurnMappingStore {
    override fun find(code: String): FuelBurnMapping? {
        val hit = (cache.get(code) as FuelBurnMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnMapping> = inner.list()
}

fun newCachedFuelBurnMappingStore(inner: FuelBurnMappingStore, ttl: Int, clock: Clock): FuelBurnMappingStore {
    return CachedFuelBurnMappingStore(
        inner = inner,
        cache = newTtlCache<FuelBurnMapping>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn mappings. Empty fields match all. */
data class FuelBurnMappingQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnMapping>): MutableList<FuelBurnMapping> {
        val matched = filter(items) { candidate: FuelBurnMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnMappings(store: FuelBurnMappingStore, query: FuelBurnMappingQuery): MutableList<FuelBurnMapping> {
    return query.apply(store.list())
}
