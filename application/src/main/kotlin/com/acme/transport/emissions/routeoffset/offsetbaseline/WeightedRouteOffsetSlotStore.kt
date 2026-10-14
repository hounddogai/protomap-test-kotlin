package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted route offset slots by code. */
interface WeightedRouteOffsetSlotStore {
    fun find(code: String): WeightedRouteOffsetSlot?
    fun save(item: WeightedRouteOffsetSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRouteOffsetSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRouteOffsetSlotStore(
    var items: MutableMap<String, WeightedRouteOffsetSlot>,
    var order: MutableList<String>,
) : WeightedRouteOffsetSlotStore {
    override fun find(code: String): WeightedRouteOffsetSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRouteOffsetSlot) {
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

    override fun list(): MutableList<WeightedRouteOffsetSlot> {
        val out = mutableListOf<WeightedRouteOffsetSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRouteOffsetSlotStore(): WeightedRouteOffsetSlotStore {
    return MemoryWeightedRouteOffsetSlotStore(
        mutableMapOf<String, WeightedRouteOffsetSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRouteOffsetSlotStore(
    var inner: WeightedRouteOffsetSlotStore,
    var cache: TtlCache<WeightedRouteOffsetSlot>,
    var hits: Int,
    var misses: Int,
) : WeightedRouteOffsetSlotStore {
    override fun find(code: String): WeightedRouteOffsetSlot? {
        val hit = (cache.get(code) as WeightedRouteOffsetSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRouteOffsetSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRouteOffsetSlot> = inner.list()
}

fun newCachedWeightedRouteOffsetSlotStore(inner: WeightedRouteOffsetSlotStore, ttl: Int, clock: Clock): WeightedRouteOffsetSlotStore {
    return CachedWeightedRouteOffsetSlotStore(
        inner = inner,
        cache = newTtlCache<WeightedRouteOffsetSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted route offset slots. Empty fields match all. */
data class WeightedRouteOffsetSlotQuery(
    var locale: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRouteOffsetSlot): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedRouteOffsetSlot>): MutableList<WeightedRouteOffsetSlot> {
        val matched = filter(items) { candidate: WeightedRouteOffsetSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRouteOffsetSlots(store: WeightedRouteOffsetSlotStore, query: WeightedRouteOffsetSlotQuery): MutableList<WeightedRouteOffsetSlot> {
    return query.apply(store.list())
}
