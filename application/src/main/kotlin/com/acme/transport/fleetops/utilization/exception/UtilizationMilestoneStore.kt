package com.acme.transport.fleetops.utilization.exception

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps utilization milestones by code. */
interface UtilizationMilestoneStore {
    fun find(code: String): UtilizationMilestone?
    fun save(item: UtilizationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<UtilizationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryUtilizationMilestoneStore(
    var items: MutableMap<String, UtilizationMilestone>,
    var order: MutableList<String>,
) : UtilizationMilestoneStore {
    override fun find(code: String): UtilizationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: UtilizationMilestone) {
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

    override fun list(): MutableList<UtilizationMilestone> {
        val out = mutableListOf<UtilizationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryUtilizationMilestoneStore(): UtilizationMilestoneStore {
    return MemoryUtilizationMilestoneStore(
        mutableMapOf<String, UtilizationMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedUtilizationMilestoneStore(
    var inner: UtilizationMilestoneStore,
    var cache: TtlCache<UtilizationMilestone>,
    var hits: Int,
    var misses: Int,
) : UtilizationMilestoneStore {
    override fun find(code: String): UtilizationMilestone? {
        val hit = (cache.get(code) as UtilizationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: UtilizationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<UtilizationMilestone> = inner.list()
}

fun newCachedUtilizationMilestoneStore(inner: UtilizationMilestoneStore, ttl: Int, clock: Clock): UtilizationMilestoneStore {
    return CachedUtilizationMilestoneStore(
        inner = inner,
        cache = newTtlCache<UtilizationMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects utilization milestones. Empty fields match all. */
data class UtilizationMilestoneQuery(
    var segment: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: UtilizationMilestone): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<UtilizationMilestone>): MutableList<UtilizationMilestone> {
        val matched = filter(items) { candidate: UtilizationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findUtilizationMilestones(store: UtilizationMilestoneStore, query: UtilizationMilestoneQuery): MutableList<UtilizationMilestone> {
    return query.apply(store.list())
}
