package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire slots by code. */
interface TireSlotStore {
    fun find(code: String): TireSlot?
    fun save(item: TireSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireSlotStore(
    var items: MutableMap<String, TireSlot>,
    var order: MutableList<String>,
) : TireSlotStore {
    override fun find(code: String): TireSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireSlot) {
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

    override fun list(): MutableList<TireSlot> {
        val out = mutableListOf<TireSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireSlotStore(): TireSlotStore {
    return MemoryTireSlotStore(mutableMapOf<String, TireSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireSlotStore(
    var inner: TireSlotStore,
    var cache: TtlCache<TireSlot>,
    var hits: Int,
    var misses: Int,
) : TireSlotStore {
    override fun find(code: String): TireSlot? {
        val hit = (cache.get(code) as TireSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireSlot> = inner.list()
}

fun newCachedTireSlotStore(inner: TireSlotStore, ttl: Int, clock: Clock): TireSlotStore {
    return CachedTireSlotStore(
        inner = inner,
        cache = newTtlCache<TireSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire slots. Empty fields match all. */
data class TireSlotQuery(
    var country: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireSlot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<TireSlot>): MutableList<TireSlot> {
        val matched = filter(items) { candidate: TireSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireSlots(store: TireSlotStore, query: TireSlotQuery): MutableList<TireSlot> = query.apply(store.list())
