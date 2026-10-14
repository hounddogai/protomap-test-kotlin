package com.acme.transport.crewing.swap.bundle

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled swap slots by code. */
interface ScheduledSwapSlotStore {
    fun find(code: String): ScheduledSwapSlot?
    fun save(item: ScheduledSwapSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledSwapSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledSwapSlotStore(
    var items: MutableMap<String, ScheduledSwapSlot>,
    var order: MutableList<String>,
) : ScheduledSwapSlotStore {
    override fun find(code: String): ScheduledSwapSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledSwapSlot) {
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

    override fun list(): MutableList<ScheduledSwapSlot> {
        val out = mutableListOf<ScheduledSwapSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledSwapSlotStore(): ScheduledSwapSlotStore {
    return MemoryScheduledSwapSlotStore(mutableMapOf<String, ScheduledSwapSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledSwapSlotStore(
    var inner: ScheduledSwapSlotStore,
    var cache: TtlCache<ScheduledSwapSlot>,
    var hits: Int,
    var misses: Int,
) : ScheduledSwapSlotStore {
    override fun find(code: String): ScheduledSwapSlot? {
        val hit = (cache.get(code) as ScheduledSwapSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledSwapSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledSwapSlot> = inner.list()
}

fun newCachedScheduledSwapSlotStore(inner: ScheduledSwapSlotStore, ttl: Int, clock: Clock): ScheduledSwapSlotStore {
    return CachedScheduledSwapSlotStore(
        inner = inner,
        cache = newTtlCache<ScheduledSwapSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled swap slots. Empty fields match all. */
data class ScheduledSwapSlotQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledSwapSlot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<ScheduledSwapSlot>): MutableList<ScheduledSwapSlot> {
        val matched = filter(items) { candidate: ScheduledSwapSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledSwapSlots(store: ScheduledSwapSlotStore, query: ScheduledSwapSlotQuery): MutableList<ScheduledSwapSlot> {
    return query.apply(store.list())
}
