package com.acme.transport.crewing.swap.group

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted swap slots by code. */
interface WeightedSwapSlotStore {
    fun find(code: String): WeightedSwapSlot?
    fun save(item: WeightedSwapSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedSwapSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedSwapSlotStore(
    var items: MutableMap<String, WeightedSwapSlot>,
    var order: MutableList<String>,
) : WeightedSwapSlotStore {
    override fun find(code: String): WeightedSwapSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedSwapSlot) {
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

    override fun list(): MutableList<WeightedSwapSlot> {
        val out = mutableListOf<WeightedSwapSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedSwapSlotStore(): WeightedSwapSlotStore {
    return MemoryWeightedSwapSlotStore(mutableMapOf<String, WeightedSwapSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedSwapSlotStore(
    var inner: WeightedSwapSlotStore,
    var cache: TtlCache<WeightedSwapSlot>,
    var hits: Int,
    var misses: Int,
) : WeightedSwapSlotStore {
    override fun find(code: String): WeightedSwapSlot? {
        val hit = (cache.get(code) as WeightedSwapSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedSwapSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedSwapSlot> = inner.list()
}

fun newCachedWeightedSwapSlotStore(inner: WeightedSwapSlotStore, ttl: Int, clock: Clock): WeightedSwapSlotStore {
    return CachedWeightedSwapSlotStore(
        inner = inner,
        cache = newTtlCache<WeightedSwapSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted swap slots. Empty fields match all. */
data class WeightedSwapSlotQuery(
    var name: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedSwapSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedSwapSlot>): MutableList<WeightedSwapSlot> {
        val matched = filter(items) { candidate: WeightedSwapSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedSwapSlots(store: WeightedSwapSlotStore, query: WeightedSwapSlotQuery): MutableList<WeightedSwapSlot> {
    return query.apply(store.list())
}
