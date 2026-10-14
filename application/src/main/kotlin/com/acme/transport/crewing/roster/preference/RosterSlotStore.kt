package com.acme.transport.crewing.roster.preference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster slots by code. */
interface RosterSlotStore {
    fun find(code: String): RosterSlot?
    fun save(item: RosterSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterSlotStore(
    var items: MutableMap<String, RosterSlot>,
    var order: MutableList<String>,
) : RosterSlotStore {
    override fun find(code: String): RosterSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterSlot) {
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

    override fun list(): MutableList<RosterSlot> {
        val out = mutableListOf<RosterSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterSlotStore(): RosterSlotStore {
    return MemoryRosterSlotStore(mutableMapOf<String, RosterSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterSlotStore(
    var inner: RosterSlotStore,
    var cache: TtlCache<RosterSlot>,
    var hits: Int,
    var misses: Int,
) : RosterSlotStore {
    override fun find(code: String): RosterSlot? {
        val hit = (cache.get(code) as RosterSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterSlot> = inner.list()
}

fun newCachedRosterSlotStore(inner: RosterSlotStore, ttl: Int, clock: Clock): RosterSlotStore {
    return CachedRosterSlotStore(
        inner = inner,
        cache = newTtlCache<RosterSlot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster slots. Empty fields match all. */
data class RosterSlotQuery(
    var category: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterSlot): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
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

    fun apply(items: MutableList<RosterSlot>): MutableList<RosterSlot> {
        val matched = filter(items) { candidate: RosterSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterSlots(store: RosterSlotStore, query: RosterSlotQuery): MutableList<RosterSlot> {
    return query.apply(store.list())
}
