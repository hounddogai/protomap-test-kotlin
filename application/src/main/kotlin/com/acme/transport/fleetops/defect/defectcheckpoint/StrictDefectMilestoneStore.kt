package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict defect milestones by code. */
interface StrictDefectMilestoneStore {
    fun find(code: String): StrictDefectMilestone?
    fun save(item: StrictDefectMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDefectMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDefectMilestoneStore(
    var items: MutableMap<String, StrictDefectMilestone>,
    var order: MutableList<String>,
) : StrictDefectMilestoneStore {
    override fun find(code: String): StrictDefectMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDefectMilestone) {
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

    override fun list(): MutableList<StrictDefectMilestone> {
        val out = mutableListOf<StrictDefectMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDefectMilestoneStore(): StrictDefectMilestoneStore {
    return MemoryStrictDefectMilestoneStore(
        mutableMapOf<String, StrictDefectMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDefectMilestoneStore(
    var inner: StrictDefectMilestoneStore,
    var cache: TtlCache<StrictDefectMilestone>,
    var hits: Int,
    var misses: Int,
) : StrictDefectMilestoneStore {
    override fun find(code: String): StrictDefectMilestone? {
        val hit = (cache.get(code) as StrictDefectMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDefectMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDefectMilestone> = inner.list()
}

fun newCachedStrictDefectMilestoneStore(inner: StrictDefectMilestoneStore, ttl: Int, clock: Clock): StrictDefectMilestoneStore {
    return CachedStrictDefectMilestoneStore(
        inner = inner,
        cache = newTtlCache<StrictDefectMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict defect milestones. Empty fields match all. */
data class StrictDefectMilestoneQuery(
    var depot: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDefectMilestone): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDefectMilestone>): MutableList<StrictDefectMilestone> {
        val matched = filter(items) { candidate: StrictDefectMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDefectMilestones(store: StrictDefectMilestoneStore, query: StrictDefectMilestoneQuery): MutableList<StrictDefectMilestone> {
    return query.apply(store.list())
}
