package com.acme.transport.emissions.fuelburn.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn milestones by code. */
interface FuelBurnMilestoneStore {
    fun find(code: String): FuelBurnMilestone?
    fun save(item: FuelBurnMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnMilestoneStore(
    var items: MutableMap<String, FuelBurnMilestone>,
    var order: MutableList<String>,
) : FuelBurnMilestoneStore {
    override fun find(code: String): FuelBurnMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnMilestone) {
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

    override fun list(): MutableList<FuelBurnMilestone> {
        val out = mutableListOf<FuelBurnMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnMilestoneStore(): FuelBurnMilestoneStore {
    return MemoryFuelBurnMilestoneStore(mutableMapOf<String, FuelBurnMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnMilestoneStore(
    var inner: FuelBurnMilestoneStore,
    var cache: TtlCache<FuelBurnMilestone>,
    var hits: Int,
    var misses: Int,
) : FuelBurnMilestoneStore {
    override fun find(code: String): FuelBurnMilestone? {
        val hit = (cache.get(code) as FuelBurnMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnMilestone> = inner.list()
}

fun newCachedFuelBurnMilestoneStore(inner: FuelBurnMilestoneStore, ttl: Int, clock: Clock): FuelBurnMilestoneStore {
    return CachedFuelBurnMilestoneStore(
        inner = inner,
        cache = newTtlCache<FuelBurnMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn milestones. Empty fields match all. */
data class FuelBurnMilestoneQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<FuelBurnMilestone>): MutableList<FuelBurnMilestone> {
        val matched = filter(items) { candidate: FuelBurnMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnMilestones(store: FuelBurnMilestoneStore, query: FuelBurnMilestoneQuery): MutableList<FuelBurnMilestone> {
    return query.apply(store.list())
}
