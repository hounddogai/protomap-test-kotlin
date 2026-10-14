package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback zone slots by code. */
interface FallbackZoneSlotStore {
    fun find(code: String): FallbackZoneSlot?
    fun save(item: FallbackZoneSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackZoneSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackZoneSlotStore(
    var items: MutableMap<String, FallbackZoneSlot>,
    var order: MutableList<String>,
) : FallbackZoneSlotStore {
    override fun find(code: String): FallbackZoneSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackZoneSlot) {
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

    override fun list(): MutableList<FallbackZoneSlot> {
        val out = mutableListOf<FallbackZoneSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackZoneSlotStore(): FallbackZoneSlotStore {
    return MemoryFallbackZoneSlotStore(mutableMapOf<String, FallbackZoneSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackZoneSlotStore(
    var inner: FallbackZoneSlotStore,
    var cache: TtlCache<FallbackZoneSlot>,
    var hits: Int,
    var misses: Int,
) : FallbackZoneSlotStore {
    override fun find(code: String): FallbackZoneSlot? {
        val hit = (cache.get(code) as FallbackZoneSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackZoneSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackZoneSlot> = inner.list()
}

fun newCachedFallbackZoneSlotStore(inner: FallbackZoneSlotStore, ttl: Int, clock: Clock): FallbackZoneSlotStore {
    return CachedFallbackZoneSlotStore(
        inner = inner,
        cache = newTtlCache<FallbackZoneSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback zone slots. Empty fields match all. */
data class FallbackZoneSlotQuery(
    var country: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackZoneSlot): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackZoneSlot>): MutableList<FallbackZoneSlot> {
        val matched = filter(items) { candidate: FallbackZoneSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackZoneSlots(store: FallbackZoneSlotStore, query: FallbackZoneSlotQuery): MutableList<FallbackZoneSlot> {
    return query.apply(store.list())
}
