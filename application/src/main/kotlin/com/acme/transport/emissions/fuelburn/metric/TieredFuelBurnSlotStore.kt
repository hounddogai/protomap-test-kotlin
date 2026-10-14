package com.acme.transport.emissions.fuelburn.metric

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered fuel burn slots by code. */
interface TieredFuelBurnSlotStore {
    fun find(code: String): TieredFuelBurnSlot?
    fun save(item: TieredFuelBurnSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredFuelBurnSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredFuelBurnSlotStore(
    var items: MutableMap<String, TieredFuelBurnSlot>,
    var order: MutableList<String>,
) : TieredFuelBurnSlotStore {
    override fun find(code: String): TieredFuelBurnSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredFuelBurnSlot) {
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

    override fun list(): MutableList<TieredFuelBurnSlot> {
        val out = mutableListOf<TieredFuelBurnSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredFuelBurnSlotStore(): TieredFuelBurnSlotStore {
    return MemoryTieredFuelBurnSlotStore(mutableMapOf<String, TieredFuelBurnSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredFuelBurnSlotStore(
    var inner: TieredFuelBurnSlotStore,
    var cache: TtlCache<TieredFuelBurnSlot>,
    var hits: Int,
    var misses: Int,
) : TieredFuelBurnSlotStore {
    override fun find(code: String): TieredFuelBurnSlot? {
        val hit = (cache.get(code) as TieredFuelBurnSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredFuelBurnSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredFuelBurnSlot> = inner.list()
}

fun newCachedTieredFuelBurnSlotStore(inner: TieredFuelBurnSlotStore, ttl: Int, clock: Clock): TieredFuelBurnSlotStore {
    return CachedTieredFuelBurnSlotStore(
        inner = inner,
        cache = newTtlCache<TieredFuelBurnSlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered fuel burn slots. Empty fields match all. */
data class TieredFuelBurnSlotQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredFuelBurnSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredFuelBurnSlot>): MutableList<TieredFuelBurnSlot> {
        val matched = filter(items) { candidate: TieredFuelBurnSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredFuelBurnSlots(store: TieredFuelBurnSlotStore, query: TieredFuelBurnSlotQuery): MutableList<TieredFuelBurnSlot> {
    return query.apply(store.list())
}
