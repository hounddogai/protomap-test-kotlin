package com.acme.transport.emissions.evcharge.batchedchargeslot

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback ev charge slots by code. */
interface FallbackEvChargeSlotStore {
    fun find(code: String): FallbackEvChargeSlot?
    fun save(item: FallbackEvChargeSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEvChargeSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEvChargeSlotStore(
    var items: MutableMap<String, FallbackEvChargeSlot>,
    var order: MutableList<String>,
) : FallbackEvChargeSlotStore {
    override fun find(code: String): FallbackEvChargeSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEvChargeSlot) {
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

    override fun list(): MutableList<FallbackEvChargeSlot> {
        val out = mutableListOf<FallbackEvChargeSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEvChargeSlotStore(): FallbackEvChargeSlotStore {
    return MemoryFallbackEvChargeSlotStore(
        mutableMapOf<String, FallbackEvChargeSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEvChargeSlotStore(
    var inner: FallbackEvChargeSlotStore,
    var cache: TtlCache<FallbackEvChargeSlot>,
    var hits: Int,
    var misses: Int,
) : FallbackEvChargeSlotStore {
    override fun find(code: String): FallbackEvChargeSlot? {
        val hit = (cache.get(code) as FallbackEvChargeSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEvChargeSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEvChargeSlot> = inner.list()
}

fun newCachedFallbackEvChargeSlotStore(inner: FallbackEvChargeSlotStore, ttl: Int, clock: Clock): FallbackEvChargeSlotStore {
    return CachedFallbackEvChargeSlotStore(
        inner = inner,
        cache = newTtlCache<FallbackEvChargeSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback ev charge slots. Empty fields match all. */
data class FallbackEvChargeSlotQuery(
    var channel: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEvChargeSlot): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackEvChargeSlot>): MutableList<FallbackEvChargeSlot> {
        val matched = filter(items) { candidate: FallbackEvChargeSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEvChargeSlots(store: FallbackEvChargeSlotStore, query: FallbackEvChargeSlotQuery): MutableList<FallbackEvChargeSlot> {
    return query.apply(store.list())
}
