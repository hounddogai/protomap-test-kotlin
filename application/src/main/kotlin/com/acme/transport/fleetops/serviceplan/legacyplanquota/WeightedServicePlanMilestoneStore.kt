package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted service plan milestones by code. */
interface WeightedServicePlanMilestoneStore {
    fun find(code: String): WeightedServicePlanMilestone?
    fun save(item: WeightedServicePlanMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedServicePlanMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedServicePlanMilestoneStore(
    var items: MutableMap<String, WeightedServicePlanMilestone>,
    var order: MutableList<String>,
) : WeightedServicePlanMilestoneStore {
    override fun find(code: String): WeightedServicePlanMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedServicePlanMilestone) {
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

    override fun list(): MutableList<WeightedServicePlanMilestone> {
        val out = mutableListOf<WeightedServicePlanMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedServicePlanMilestoneStore(): WeightedServicePlanMilestoneStore {
    return MemoryWeightedServicePlanMilestoneStore(
        mutableMapOf<String, WeightedServicePlanMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedServicePlanMilestoneStore(
    var inner: WeightedServicePlanMilestoneStore,
    var cache: TtlCache<WeightedServicePlanMilestone>,
    var hits: Int,
    var misses: Int,
) : WeightedServicePlanMilestoneStore {
    override fun find(code: String): WeightedServicePlanMilestone? {
        val hit = (cache.get(code) as WeightedServicePlanMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedServicePlanMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedServicePlanMilestone> = inner.list()
}

fun newCachedWeightedServicePlanMilestoneStore(inner: WeightedServicePlanMilestoneStore, ttl: Int, clock: Clock): WeightedServicePlanMilestoneStore {
    return CachedWeightedServicePlanMilestoneStore(
        inner = inner,
        cache = newTtlCache<WeightedServicePlanMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted service plan milestones. Empty fields match all. */
data class WeightedServicePlanMilestoneQuery(
    var label: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedServicePlanMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<WeightedServicePlanMilestone>): MutableList<WeightedServicePlanMilestone> {
        val matched = filter(items) { candidate: WeightedServicePlanMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedServicePlanMilestones(store: WeightedServicePlanMilestoneStore, query: WeightedServicePlanMilestoneQuery): MutableList<WeightedServicePlanMilestone> {
    return query.apply(store.list())
}
