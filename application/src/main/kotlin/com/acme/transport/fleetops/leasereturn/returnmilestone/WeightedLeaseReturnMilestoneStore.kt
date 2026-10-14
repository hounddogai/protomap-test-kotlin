package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted lease return milestones by code. */
interface WeightedLeaseReturnMilestoneStore {
    fun find(code: String): WeightedLeaseReturnMilestone?
    fun save(item: WeightedLeaseReturnMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedLeaseReturnMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedLeaseReturnMilestoneStore(
    var items: MutableMap<String, WeightedLeaseReturnMilestone>,
    var order: MutableList<String>,
) : WeightedLeaseReturnMilestoneStore {
    override fun find(code: String): WeightedLeaseReturnMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedLeaseReturnMilestone) {
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

    override fun list(): MutableList<WeightedLeaseReturnMilestone> {
        val out = mutableListOf<WeightedLeaseReturnMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedLeaseReturnMilestoneStore(): WeightedLeaseReturnMilestoneStore {
    return MemoryWeightedLeaseReturnMilestoneStore(
        mutableMapOf<String, WeightedLeaseReturnMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedLeaseReturnMilestoneStore(
    var inner: WeightedLeaseReturnMilestoneStore,
    var cache: TtlCache<WeightedLeaseReturnMilestone>,
    var hits: Int,
    var misses: Int,
) : WeightedLeaseReturnMilestoneStore {
    override fun find(code: String): WeightedLeaseReturnMilestone? {
        val hit = (cache.get(code) as WeightedLeaseReturnMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedLeaseReturnMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedLeaseReturnMilestone> = inner.list()
}

fun newCachedWeightedLeaseReturnMilestoneStore(inner: WeightedLeaseReturnMilestoneStore, ttl: Int, clock: Clock): WeightedLeaseReturnMilestoneStore {
    return CachedWeightedLeaseReturnMilestoneStore(
        inner = inner,
        cache = newTtlCache<WeightedLeaseReturnMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted lease return milestones. Empty fields match all. */
data class WeightedLeaseReturnMilestoneQuery(
    var channel: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedLeaseReturnMilestone): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedLeaseReturnMilestone>): MutableList<WeightedLeaseReturnMilestone> {
        val matched = filter(items) { candidate: WeightedLeaseReturnMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedLeaseReturnMilestones(store: WeightedLeaseReturnMilestoneStore, query: WeightedLeaseReturnMilestoneQuery): MutableList<WeightedLeaseReturnMilestone> {
    return query.apply(store.list())
}
