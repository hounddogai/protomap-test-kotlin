package com.acme.transport.freightpay.accessorial.accessorialadjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional accessorial slots by code. */
interface RegionalAccessorialSlotStore {
    fun find(code: String): RegionalAccessorialSlot?
    fun save(item: RegionalAccessorialSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalAccessorialSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalAccessorialSlotStore(
    var items: MutableMap<String, RegionalAccessorialSlot>,
    var order: MutableList<String>,
) : RegionalAccessorialSlotStore {
    override fun find(code: String): RegionalAccessorialSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalAccessorialSlot) {
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

    override fun list(): MutableList<RegionalAccessorialSlot> {
        val out = mutableListOf<RegionalAccessorialSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalAccessorialSlotStore(): RegionalAccessorialSlotStore {
    return MemoryRegionalAccessorialSlotStore(
        mutableMapOf<String, RegionalAccessorialSlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalAccessorialSlotStore(
    var inner: RegionalAccessorialSlotStore,
    var cache: TtlCache<RegionalAccessorialSlot>,
    var hits: Int,
    var misses: Int,
) : RegionalAccessorialSlotStore {
    override fun find(code: String): RegionalAccessorialSlot? {
        val hit = (cache.get(code) as RegionalAccessorialSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalAccessorialSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalAccessorialSlot> = inner.list()
}

fun newCachedRegionalAccessorialSlotStore(inner: RegionalAccessorialSlotStore, ttl: Int, clock: Clock): RegionalAccessorialSlotStore {
    return CachedRegionalAccessorialSlotStore(
        inner = inner,
        cache = newTtlCache<RegionalAccessorialSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional accessorial slots. Empty fields match all. */
data class RegionalAccessorialSlotQuery(
    var lane: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalAccessorialSlot): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<RegionalAccessorialSlot>): MutableList<RegionalAccessorialSlot> {
        val matched = filter(items) { candidate: RegionalAccessorialSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalAccessorialSlots(store: RegionalAccessorialSlotStore, query: RegionalAccessorialSlotQuery): MutableList<RegionalAccessorialSlot> {
    return query.apply(store.list())
}
