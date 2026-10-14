package com.acme.transport.crewing.dutyperiod.milestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive duty period milestones by code. */
interface AdaptiveDutyPeriodMilestoneStore {
    fun find(code: String): AdaptiveDutyPeriodMilestone?
    fun save(item: AdaptiveDutyPeriodMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveDutyPeriodMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveDutyPeriodMilestoneStore(
    var items: MutableMap<String, AdaptiveDutyPeriodMilestone>,
    var order: MutableList<String>,
) : AdaptiveDutyPeriodMilestoneStore {
    override fun find(code: String): AdaptiveDutyPeriodMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveDutyPeriodMilestone) {
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

    override fun list(): MutableList<AdaptiveDutyPeriodMilestone> {
        val out = mutableListOf<AdaptiveDutyPeriodMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveDutyPeriodMilestoneStore(): AdaptiveDutyPeriodMilestoneStore {
    return MemoryAdaptiveDutyPeriodMilestoneStore(
        mutableMapOf<String, AdaptiveDutyPeriodMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveDutyPeriodMilestoneStore(
    var inner: AdaptiveDutyPeriodMilestoneStore,
    var cache: TtlCache<AdaptiveDutyPeriodMilestone>,
    var hits: Int,
    var misses: Int,
) : AdaptiveDutyPeriodMilestoneStore {
    override fun find(code: String): AdaptiveDutyPeriodMilestone? {
        val hit = (cache.get(code) as AdaptiveDutyPeriodMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveDutyPeriodMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveDutyPeriodMilestone> = inner.list()
}

fun newCachedAdaptiveDutyPeriodMilestoneStore(inner: AdaptiveDutyPeriodMilestoneStore, ttl: Int, clock: Clock): AdaptiveDutyPeriodMilestoneStore {
    return CachedAdaptiveDutyPeriodMilestoneStore(
        inner = inner,
        cache = newTtlCache<AdaptiveDutyPeriodMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive duty period milestones. Empty fields match all. */
data class AdaptiveDutyPeriodMilestoneQuery(
    var title: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveDutyPeriodMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AdaptiveDutyPeriodMilestone>): MutableList<AdaptiveDutyPeriodMilestone> {
        val matched = filter(items) { candidate: AdaptiveDutyPeriodMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveDutyPeriodMilestones(store: AdaptiveDutyPeriodMilestoneStore, query: AdaptiveDutyPeriodMilestoneQuery): MutableList<AdaptiveDutyPeriodMilestone> {
    return query.apply(store.list())
}
