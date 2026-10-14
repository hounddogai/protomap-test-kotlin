package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps seasonal capacity slots by code. */
interface SeasonalCapacitySlotStore {
    fun find(code: String): SeasonalCapacitySlot?
    fun save(item: SeasonalCapacitySlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<SeasonalCapacitySlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySeasonalCapacitySlotStore(
    var items: MutableMap<String, SeasonalCapacitySlot>,
    var order: MutableList<String>,
) : SeasonalCapacitySlotStore {
    override fun find(code: String): SeasonalCapacitySlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SeasonalCapacitySlot) {
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

    override fun list(): MutableList<SeasonalCapacitySlot> {
        val out = mutableListOf<SeasonalCapacitySlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySeasonalCapacitySlotStore(): SeasonalCapacitySlotStore {
    return MemorySeasonalCapacitySlotStore(
        mutableMapOf<String, SeasonalCapacitySlot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSeasonalCapacitySlotStore(
    var inner: SeasonalCapacitySlotStore,
    var cache: TtlCache<SeasonalCapacitySlot>,
    var hits: Int,
    var misses: Int,
) : SeasonalCapacitySlotStore {
    override fun find(code: String): SeasonalCapacitySlot? {
        val hit = (cache.get(code) as SeasonalCapacitySlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SeasonalCapacitySlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SeasonalCapacitySlot> = inner.list()
}

fun newCachedSeasonalCapacitySlotStore(inner: SeasonalCapacitySlotStore, ttl: Int, clock: Clock): SeasonalCapacitySlotStore {
    return CachedSeasonalCapacitySlotStore(
        inner = inner,
        cache = newTtlCache<SeasonalCapacitySlot>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects seasonal capacity slots. Empty fields match all. */
data class SeasonalCapacitySlotQuery(
    var region: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SeasonalCapacitySlot): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SeasonalCapacitySlot>): MutableList<SeasonalCapacitySlot> {
        val matched = filter(items) { candidate: SeasonalCapacitySlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSeasonalCapacitySlots(store: SeasonalCapacitySlotStore, query: SeasonalCapacitySlotQuery): MutableList<SeasonalCapacitySlot> {
    return query.apply(store.list())
}
