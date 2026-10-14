package com.acme.transport.terminal.crossdock.strictdockslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted cross dock slots by code. */
interface WeightedCrossDockSlotStore {
    fun find(code: String): WeightedCrossDockSlot?
    fun save(item: WeightedCrossDockSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedCrossDockSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedCrossDockSlotStore(
    var items: MutableMap<String, WeightedCrossDockSlot>,
    var order: MutableList<String>,
) : WeightedCrossDockSlotStore {
    override fun find(code: String): WeightedCrossDockSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedCrossDockSlot) {
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

    override fun list(): MutableList<WeightedCrossDockSlot> {
        val out = mutableListOf<WeightedCrossDockSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedCrossDockSlotStore(): WeightedCrossDockSlotStore {
    return MemoryWeightedCrossDockSlotStore(
        mutableMapOf<String, WeightedCrossDockSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedCrossDockSlotStore(
    var inner: WeightedCrossDockSlotStore,
    var cache: TtlCache<WeightedCrossDockSlot>,
    var hits: Int,
    var misses: Int,
) : WeightedCrossDockSlotStore {
    override fun find(code: String): WeightedCrossDockSlot? {
        val hit = (cache.get(code) as WeightedCrossDockSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedCrossDockSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedCrossDockSlot> = inner.list()
}

fun newCachedWeightedCrossDockSlotStore(inner: WeightedCrossDockSlotStore, ttl: Int, clock: Clock): WeightedCrossDockSlotStore {
    return CachedWeightedCrossDockSlotStore(
        inner = inner,
        cache = newTtlCache<WeightedCrossDockSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted cross dock slots. Empty fields match all. */
data class WeightedCrossDockSlotQuery(
    var name: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedCrossDockSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<WeightedCrossDockSlot>): MutableList<WeightedCrossDockSlot> {
        val matched = filter(items) { candidate: WeightedCrossDockSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedCrossDockSlots(store: WeightedCrossDockSlotStore, query: WeightedCrossDockSlotQuery): MutableList<WeightedCrossDockSlot> {
    return query.apply(store.list())
}
