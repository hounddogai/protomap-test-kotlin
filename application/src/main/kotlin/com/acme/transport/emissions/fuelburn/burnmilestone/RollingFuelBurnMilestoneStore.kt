package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rolling fuel burn milestones by code. */
interface RollingFuelBurnMilestoneStore {
    fun find(code: String): RollingFuelBurnMilestone?
    fun save(item: RollingFuelBurnMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingFuelBurnMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingFuelBurnMilestoneStore(
    var items: MutableMap<String, RollingFuelBurnMilestone>,
    var order: MutableList<String>,
) : RollingFuelBurnMilestoneStore {
    override fun find(code: String): RollingFuelBurnMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingFuelBurnMilestone) {
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

    override fun list(): MutableList<RollingFuelBurnMilestone> {
        val out = mutableListOf<RollingFuelBurnMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingFuelBurnMilestoneStore(): RollingFuelBurnMilestoneStore {
    return MemoryRollingFuelBurnMilestoneStore(
        mutableMapOf<String, RollingFuelBurnMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingFuelBurnMilestoneStore(
    var inner: RollingFuelBurnMilestoneStore,
    var cache: TtlCache<RollingFuelBurnMilestone>,
    var hits: Int,
    var misses: Int,
) : RollingFuelBurnMilestoneStore {
    override fun find(code: String): RollingFuelBurnMilestone? {
        val hit = (cache.get(code) as RollingFuelBurnMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingFuelBurnMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingFuelBurnMilestone> = inner.list()
}

fun newCachedRollingFuelBurnMilestoneStore(inner: RollingFuelBurnMilestoneStore, ttl: Int, clock: Clock): RollingFuelBurnMilestoneStore {
    return CachedRollingFuelBurnMilestoneStore(
        inner = inner,
        cache = newTtlCache<RollingFuelBurnMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling fuel burn milestones. Empty fields match all. */
data class RollingFuelBurnMilestoneQuery(
    var title: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingFuelBurnMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<RollingFuelBurnMilestone>): MutableList<RollingFuelBurnMilestone> {
        val matched = filter(items) { candidate: RollingFuelBurnMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingFuelBurnMilestones(store: RollingFuelBurnMilestoneStore, query: RollingFuelBurnMilestoneQuery): MutableList<RollingFuelBurnMilestone> {
    return query.apply(store.list())
}
