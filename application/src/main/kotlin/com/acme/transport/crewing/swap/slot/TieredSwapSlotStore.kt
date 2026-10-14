package com.acme.transport.crewing.swap.slot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered swap slots by code. */
interface TieredSwapSlotStore {
    fun find(code: String): TieredSwapSlot?
    fun save(item: TieredSwapSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredSwapSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredSwapSlotStore(
    var items: MutableMap<String, TieredSwapSlot>,
    var order: MutableList<String>,
) : TieredSwapSlotStore {
    override fun find(code: String): TieredSwapSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredSwapSlot) {
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

    override fun list(): MutableList<TieredSwapSlot> {
        val out = mutableListOf<TieredSwapSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredSwapSlotStore(): TieredSwapSlotStore {
    return MemoryTieredSwapSlotStore(mutableMapOf<String, TieredSwapSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredSwapSlotStore(
    var inner: TieredSwapSlotStore,
    var cache: TtlCache<TieredSwapSlot>,
    var hits: Int,
    var misses: Int,
) : TieredSwapSlotStore {
    override fun find(code: String): TieredSwapSlot? {
        val hit = (cache.get(code) as TieredSwapSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredSwapSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredSwapSlot> = inner.list()
}

fun newCachedTieredSwapSlotStore(inner: TieredSwapSlotStore, ttl: Int, clock: Clock): TieredSwapSlotStore {
    return CachedTieredSwapSlotStore(
        inner = inner,
        cache = newTtlCache<TieredSwapSlot>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered swap slots. Empty fields match all. */
data class TieredSwapSlotQuery(
    var label: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredSwapSlot): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredSwapSlot>): MutableList<TieredSwapSlot> {
        val matched = filter(items) { candidate: TieredSwapSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredSwapSlots(store: TieredSwapSlotStore, query: TieredSwapSlotQuery): MutableList<TieredSwapSlot> {
    return query.apply(store.list())
}
