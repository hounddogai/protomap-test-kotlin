package com.acme.transport.emissions.emissionfactor.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor slots by code. */
interface EmissionFactorSlotStore {
    fun find(code: String): EmissionFactorSlot?
    fun save(item: EmissionFactorSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorSlotStore(
    var items: MutableMap<String, EmissionFactorSlot>,
    var order: MutableList<String>,
) : EmissionFactorSlotStore {
    override fun find(code: String): EmissionFactorSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorSlot) {
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

    override fun list(): MutableList<EmissionFactorSlot> {
        val out = mutableListOf<EmissionFactorSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorSlotStore(): EmissionFactorSlotStore {
    return MemoryEmissionFactorSlotStore(mutableMapOf<String, EmissionFactorSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorSlotStore(
    var inner: EmissionFactorSlotStore,
    var cache: TtlCache<EmissionFactorSlot>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorSlotStore {
    override fun find(code: String): EmissionFactorSlot? {
        val hit = (cache.get(code) as EmissionFactorSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorSlot> = inner.list()
}

fun newCachedEmissionFactorSlotStore(inner: EmissionFactorSlotStore, ttl: Int, clock: Clock): EmissionFactorSlotStore {
    return CachedEmissionFactorSlotStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor slots. Empty fields match all. */
data class EmissionFactorSlotQuery(
    var depot: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorSlot): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorSlot>): MutableList<EmissionFactorSlot> {
        val matched = filter(items) { candidate: EmissionFactorSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorSlots(store: EmissionFactorSlotStore, query: EmissionFactorSlotQuery): MutableList<EmissionFactorSlot> {
    return query.apply(store.list())
}
