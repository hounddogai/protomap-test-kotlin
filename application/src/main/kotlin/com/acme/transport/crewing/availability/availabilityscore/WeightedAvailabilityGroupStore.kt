package com.acme.transport.crewing.availability.availabilityscore

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted availability groups by code. */
interface WeightedAvailabilityGroupStore {
    fun find(code: String): WeightedAvailabilityGroup?
    fun save(item: WeightedAvailabilityGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAvailabilityGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAvailabilityGroupStore(
    var items: MutableMap<String, WeightedAvailabilityGroup>,
    var order: MutableList<String>,
) : WeightedAvailabilityGroupStore {
    override fun find(code: String): WeightedAvailabilityGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAvailabilityGroup) {
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

    override fun list(): MutableList<WeightedAvailabilityGroup> {
        val out = mutableListOf<WeightedAvailabilityGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAvailabilityGroupStore(): WeightedAvailabilityGroupStore {
    return MemoryWeightedAvailabilityGroupStore(
        mutableMapOf<String, WeightedAvailabilityGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAvailabilityGroupStore(
    var inner: WeightedAvailabilityGroupStore,
    var cache: TtlCache<WeightedAvailabilityGroup>,
    var hits: Int,
    var misses: Int,
) : WeightedAvailabilityGroupStore {
    override fun find(code: String): WeightedAvailabilityGroup? {
        val hit = (cache.get(code) as WeightedAvailabilityGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAvailabilityGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAvailabilityGroup> = inner.list()
}

fun newCachedWeightedAvailabilityGroupStore(inner: WeightedAvailabilityGroupStore, ttl: Int, clock: Clock): WeightedAvailabilityGroupStore {
    return CachedWeightedAvailabilityGroupStore(
        inner = inner,
        cache = newTtlCache<WeightedAvailabilityGroup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted availability groups. Empty fields match all. */
data class WeightedAvailabilityGroupQuery(
    var name: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAvailabilityGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAvailabilityGroup>): MutableList<WeightedAvailabilityGroup> {
        val matched = filter(items) { candidate: WeightedAvailabilityGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAvailabilityGroups(store: WeightedAvailabilityGroupStore, query: WeightedAvailabilityGroupQuery): MutableList<WeightedAvailabilityGroup> {
    return query.apply(store.list())
}
