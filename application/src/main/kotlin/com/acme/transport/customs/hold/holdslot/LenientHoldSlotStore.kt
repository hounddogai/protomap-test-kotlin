package com.acme.transport.customs.hold.holdslot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient hold slots by code. */
interface LenientHoldSlotStore {
    fun find(code: String): LenientHoldSlot?
    fun save(item: LenientHoldSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientHoldSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientHoldSlotStore(
    var items: MutableMap<String, LenientHoldSlot>,
    var order: MutableList<String>,
) : LenientHoldSlotStore {
    override fun find(code: String): LenientHoldSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientHoldSlot) {
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

    override fun list(): MutableList<LenientHoldSlot> {
        val out = mutableListOf<LenientHoldSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientHoldSlotStore(): LenientHoldSlotStore {
    return MemoryLenientHoldSlotStore(mutableMapOf<String, LenientHoldSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientHoldSlotStore(
    var inner: LenientHoldSlotStore,
    var cache: TtlCache<LenientHoldSlot>,
    var hits: Int,
    var misses: Int,
) : LenientHoldSlotStore {
    override fun find(code: String): LenientHoldSlot? {
        val hit = (cache.get(code) as LenientHoldSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientHoldSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientHoldSlot> = inner.list()
}

fun newCachedLenientHoldSlotStore(inner: LenientHoldSlotStore, ttl: Int, clock: Clock): LenientHoldSlotStore {
    return CachedLenientHoldSlotStore(
        inner = inner,
        cache = newTtlCache<LenientHoldSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient hold slots. Empty fields match all. */
data class LenientHoldSlotQuery(
    var lane: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientHoldSlot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientHoldSlot>): MutableList<LenientHoldSlot> {
        val matched = filter(items) { candidate: LenientHoldSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientHoldSlots(store: LenientHoldSlotStore, query: LenientHoldSlotQuery): MutableList<LenientHoldSlot> {
    return query.apply(store.list())
}
