package com.acme.transport.fleetops.leasereturn.milestone

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lease return milestones by code. */
interface LeaseReturnMilestoneStore {
    fun find(code: String): LeaseReturnMilestone?
    fun save(item: LeaseReturnMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<LeaseReturnMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLeaseReturnMilestoneStore(
    var items: MutableMap<String, LeaseReturnMilestone>,
    var order: MutableList<String>,
) : LeaseReturnMilestoneStore {
    override fun find(code: String): LeaseReturnMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LeaseReturnMilestone) {
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

    override fun list(): MutableList<LeaseReturnMilestone> {
        val out = mutableListOf<LeaseReturnMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLeaseReturnMilestoneStore(): LeaseReturnMilestoneStore {
    return MemoryLeaseReturnMilestoneStore(
        mutableMapOf<String, LeaseReturnMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLeaseReturnMilestoneStore(
    var inner: LeaseReturnMilestoneStore,
    var cache: TtlCache<LeaseReturnMilestone>,
    var hits: Int,
    var misses: Int,
) : LeaseReturnMilestoneStore {
    override fun find(code: String): LeaseReturnMilestone? {
        val hit = (cache.get(code) as LeaseReturnMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LeaseReturnMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LeaseReturnMilestone> = inner.list()
}

fun newCachedLeaseReturnMilestoneStore(inner: LeaseReturnMilestoneStore, ttl: Int, clock: Clock): LeaseReturnMilestoneStore {
    return CachedLeaseReturnMilestoneStore(
        inner = inner,
        cache = newTtlCache<LeaseReturnMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lease return milestones. Empty fields match all. */
data class LeaseReturnMilestoneQuery(
    var name: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LeaseReturnMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<LeaseReturnMilestone>): MutableList<LeaseReturnMilestone> {
        val matched = filter(items) { candidate: LeaseReturnMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLeaseReturnMilestones(store: LeaseReturnMilestoneStore, query: LeaseReturnMilestoneQuery): MutableList<LeaseReturnMilestone> {
    return query.apply(store.list())
}
