package com.acme.transport.crewing.swap.plan

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap slots by code. */
interface SwapSlotStore {
    fun find(code: String): SwapSlot?
    fun save(item: SwapSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapSlotStore(
    var items: MutableMap<String, SwapSlot>,
    var order: MutableList<String>,
) : SwapSlotStore {
    override fun find(code: String): SwapSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapSlot) {
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

    override fun list(): MutableList<SwapSlot> {
        val out = mutableListOf<SwapSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapSlotStore(): SwapSlotStore {
    return MemorySwapSlotStore(mutableMapOf<String, SwapSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapSlotStore(
    var inner: SwapSlotStore,
    var cache: TtlCache<SwapSlot>,
    var hits: Int,
    var misses: Int,
) : SwapSlotStore {
    override fun find(code: String): SwapSlot? {
        val hit = (cache.get(code) as SwapSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapSlot> = inner.list()
}

fun newCachedSwapSlotStore(inner: SwapSlotStore, ttl: Int, clock: Clock): SwapSlotStore {
    return CachedSwapSlotStore(
        inner = inner,
        cache = newTtlCache<SwapSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap slots. Empty fields match all. */
data class SwapSlotQuery(
    var title: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapSlot>): MutableList<SwapSlot> {
        val matched = filter(items) { candidate: SwapSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapSlots(store: SwapSlotStore, query: SwapSlotQuery): MutableList<SwapSlot> = query.apply(store.list())
