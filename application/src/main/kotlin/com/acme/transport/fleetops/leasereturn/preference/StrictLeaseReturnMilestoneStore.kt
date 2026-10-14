package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict lease return milestones by code. */
interface StrictLeaseReturnMilestoneStore {
    fun find(code: String): StrictLeaseReturnMilestone?
    fun save(item: StrictLeaseReturnMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictLeaseReturnMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictLeaseReturnMilestoneStore(
    var items: MutableMap<String, StrictLeaseReturnMilestone>,
    var order: MutableList<String>,
) : StrictLeaseReturnMilestoneStore {
    override fun find(code: String): StrictLeaseReturnMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictLeaseReturnMilestone) {
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

    override fun list(): MutableList<StrictLeaseReturnMilestone> {
        val out = mutableListOf<StrictLeaseReturnMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictLeaseReturnMilestoneStore(): StrictLeaseReturnMilestoneStore {
    return MemoryStrictLeaseReturnMilestoneStore(
        mutableMapOf<String, StrictLeaseReturnMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictLeaseReturnMilestoneStore(
    var inner: StrictLeaseReturnMilestoneStore,
    var cache: TtlCache<StrictLeaseReturnMilestone>,
    var hits: Int,
    var misses: Int,
) : StrictLeaseReturnMilestoneStore {
    override fun find(code: String): StrictLeaseReturnMilestone? {
        val hit = (cache.get(code) as StrictLeaseReturnMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictLeaseReturnMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictLeaseReturnMilestone> = inner.list()
}

fun newCachedStrictLeaseReturnMilestoneStore(inner: StrictLeaseReturnMilestoneStore, ttl: Int, clock: Clock): StrictLeaseReturnMilestoneStore {
    return CachedStrictLeaseReturnMilestoneStore(
        inner = inner,
        cache = newTtlCache<StrictLeaseReturnMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict lease return milestones. Empty fields match all. */
data class StrictLeaseReturnMilestoneQuery(
    var segment: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictLeaseReturnMilestone): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictLeaseReturnMilestone>): MutableList<StrictLeaseReturnMilestone> {
        val matched = filter(items) { candidate: StrictLeaseReturnMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictLeaseReturnMilestones(store: StrictLeaseReturnMilestoneStore, query: StrictLeaseReturnMilestoneQuery): MutableList<StrictLeaseReturnMilestone> {
    return query.apply(store.list())
}
