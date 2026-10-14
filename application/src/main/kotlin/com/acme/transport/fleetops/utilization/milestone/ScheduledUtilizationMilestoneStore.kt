package com.acme.transport.fleetops.utilization.milestone

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled utilization milestones by code. */
interface ScheduledUtilizationMilestoneStore {
    fun find(code: String): ScheduledUtilizationMilestone?
    fun save(item: ScheduledUtilizationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledUtilizationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledUtilizationMilestoneStore(
    var items: MutableMap<String, ScheduledUtilizationMilestone>,
    var order: MutableList<String>,
) : ScheduledUtilizationMilestoneStore {
    override fun find(code: String): ScheduledUtilizationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledUtilizationMilestone) {
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

    override fun list(): MutableList<ScheduledUtilizationMilestone> {
        val out = mutableListOf<ScheduledUtilizationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledUtilizationMilestoneStore(): ScheduledUtilizationMilestoneStore {
    return MemoryScheduledUtilizationMilestoneStore(
        mutableMapOf<String, ScheduledUtilizationMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledUtilizationMilestoneStore(
    var inner: ScheduledUtilizationMilestoneStore,
    var cache: TtlCache<ScheduledUtilizationMilestone>,
    var hits: Int,
    var misses: Int,
) : ScheduledUtilizationMilestoneStore {
    override fun find(code: String): ScheduledUtilizationMilestone? {
        val hit = (cache.get(code) as ScheduledUtilizationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledUtilizationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledUtilizationMilestone> = inner.list()
}

fun newCachedScheduledUtilizationMilestoneStore(inner: ScheduledUtilizationMilestoneStore, ttl: Int, clock: Clock): ScheduledUtilizationMilestoneStore {
    return CachedScheduledUtilizationMilestoneStore(
        inner = inner,
        cache = newTtlCache<ScheduledUtilizationMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled utilization milestones. Empty fields match all. */
data class ScheduledUtilizationMilestoneQuery(
    var category: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledUtilizationMilestone): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledUtilizationMilestone>): MutableList<ScheduledUtilizationMilestone> {
        val matched = filter(items) { candidate: ScheduledUtilizationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledUtilizationMilestones(store: ScheduledUtilizationMilestoneStore, query: ScheduledUtilizationMilestoneQuery): MutableList<ScheduledUtilizationMilestone> {
    return query.apply(store.list())
}
