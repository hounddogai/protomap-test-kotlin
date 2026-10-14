package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps legacy duty period milestones by code. */
interface LegacyDutyPeriodMilestoneStore {
    fun find(code: String): LegacyDutyPeriodMilestone?
    fun save(item: LegacyDutyPeriodMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDutyPeriodMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDutyPeriodMilestoneStore(
    var items: MutableMap<String, LegacyDutyPeriodMilestone>,
    var order: MutableList<String>,
) : LegacyDutyPeriodMilestoneStore {
    override fun find(code: String): LegacyDutyPeriodMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDutyPeriodMilestone) {
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

    override fun list(): MutableList<LegacyDutyPeriodMilestone> {
        val out = mutableListOf<LegacyDutyPeriodMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDutyPeriodMilestoneStore(): LegacyDutyPeriodMilestoneStore {
    return MemoryLegacyDutyPeriodMilestoneStore(
        mutableMapOf<String, LegacyDutyPeriodMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDutyPeriodMilestoneStore(
    var inner: LegacyDutyPeriodMilestoneStore,
    var cache: TtlCache<LegacyDutyPeriodMilestone>,
    var hits: Int,
    var misses: Int,
) : LegacyDutyPeriodMilestoneStore {
    override fun find(code: String): LegacyDutyPeriodMilestone? {
        val hit = (cache.get(code) as LegacyDutyPeriodMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDutyPeriodMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDutyPeriodMilestone> = inner.list()
}

fun newCachedLegacyDutyPeriodMilestoneStore(inner: LegacyDutyPeriodMilestoneStore, ttl: Int, clock: Clock): LegacyDutyPeriodMilestoneStore {
    return CachedLegacyDutyPeriodMilestoneStore(
        inner = inner,
        cache = newTtlCache<LegacyDutyPeriodMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy duty period milestones. Empty fields match all. */
data class LegacyDutyPeriodMilestoneQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDutyPeriodMilestone): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDutyPeriodMilestone>): MutableList<LegacyDutyPeriodMilestone> {
        val matched = filter(items) { candidate: LegacyDutyPeriodMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDutyPeriodMilestones(store: LegacyDutyPeriodMilestoneStore, query: LegacyDutyPeriodMilestoneQuery): MutableList<LegacyDutyPeriodMilestone> {
    return query.apply(store.list())
}
