package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn slots by code. */
interface FuelBurnSlotStore {
    fun find(code: String): FuelBurnSlot?
    fun save(item: FuelBurnSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnSlotStore(
    var items: MutableMap<String, FuelBurnSlot>,
    var order: MutableList<String>,
) : FuelBurnSlotStore {
    override fun find(code: String): FuelBurnSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnSlot) {
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

    override fun list(): MutableList<FuelBurnSlot> {
        val out = mutableListOf<FuelBurnSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnSlotStore(): FuelBurnSlotStore {
    return MemoryFuelBurnSlotStore(mutableMapOf<String, FuelBurnSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnSlotStore(
    var inner: FuelBurnSlotStore,
    var cache: TtlCache<FuelBurnSlot>,
    var hits: Int,
    var misses: Int,
) : FuelBurnSlotStore {
    override fun find(code: String): FuelBurnSlot? {
        val hit = (cache.get(code) as FuelBurnSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnSlot> = inner.list()
}

fun newCachedFuelBurnSlotStore(inner: FuelBurnSlotStore, ttl: Int, clock: Clock): FuelBurnSlotStore {
    return CachedFuelBurnSlotStore(
        inner = inner,
        cache = newTtlCache<FuelBurnSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn slots. Empty fields match all. */
data class FuelBurnSlotQuery(
    var depot: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnSlot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelBurnSlot>): MutableList<FuelBurnSlot> {
        val matched = filter(items) { candidate: FuelBurnSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnSlots(store: FuelBurnSlotStore, query: FuelBurnSlotQuery): MutableList<FuelBurnSlot> {
    return query.apply(store.list())
}
