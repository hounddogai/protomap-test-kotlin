package com.acme.transport.crewing.roster.slot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted roster slots by code. */
interface WeightedRosterSlotStore {
    fun find(code: String): WeightedRosterSlot?
    fun save(item: WeightedRosterSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRosterSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRosterSlotStore(
    var items: MutableMap<String, WeightedRosterSlot>,
    var order: MutableList<String>,
) : WeightedRosterSlotStore {
    override fun find(code: String): WeightedRosterSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRosterSlot) {
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

    override fun list(): MutableList<WeightedRosterSlot> {
        val out = mutableListOf<WeightedRosterSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRosterSlotStore(): WeightedRosterSlotStore {
    return MemoryWeightedRosterSlotStore(mutableMapOf<String, WeightedRosterSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRosterSlotStore(
    var inner: WeightedRosterSlotStore,
    var cache: TtlCache<WeightedRosterSlot>,
    var hits: Int,
    var misses: Int,
) : WeightedRosterSlotStore {
    override fun find(code: String): WeightedRosterSlot? {
        val hit = (cache.get(code) as WeightedRosterSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRosterSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRosterSlot> = inner.list()
}

fun newCachedWeightedRosterSlotStore(inner: WeightedRosterSlotStore, ttl: Int, clock: Clock): WeightedRosterSlotStore {
    return CachedWeightedRosterSlotStore(
        inner = inner,
        cache = newTtlCache<WeightedRosterSlot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted roster slots. Empty fields match all. */
data class WeightedRosterSlotQuery(
    var title: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRosterSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<WeightedRosterSlot>): MutableList<WeightedRosterSlot> {
        val matched = filter(items) { candidate: WeightedRosterSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRosterSlots(store: WeightedRosterSlotStore, query: WeightedRosterSlotQuery): MutableList<WeightedRosterSlot> {
    return query.apply(store.list())
}
