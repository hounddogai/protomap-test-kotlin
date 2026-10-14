package com.acme.transport.crewing.dutyperiod.periodsnapshot

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps duty period milestones by code. */
interface DutyPeriodMilestoneStore {
    fun find(code: String): DutyPeriodMilestone?
    fun save(item: DutyPeriodMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<DutyPeriodMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDutyPeriodMilestoneStore(
    var items: MutableMap<String, DutyPeriodMilestone>,
    var order: MutableList<String>,
) : DutyPeriodMilestoneStore {
    override fun find(code: String): DutyPeriodMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DutyPeriodMilestone) {
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

    override fun list(): MutableList<DutyPeriodMilestone> {
        val out = mutableListOf<DutyPeriodMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDutyPeriodMilestoneStore(): DutyPeriodMilestoneStore {
    return MemoryDutyPeriodMilestoneStore(mutableMapOf<String, DutyPeriodMilestone>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDutyPeriodMilestoneStore(
    var inner: DutyPeriodMilestoneStore,
    var cache: TtlCache<DutyPeriodMilestone>,
    var hits: Int,
    var misses: Int,
) : DutyPeriodMilestoneStore {
    override fun find(code: String): DutyPeriodMilestone? {
        val hit = (cache.get(code) as DutyPeriodMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DutyPeriodMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DutyPeriodMilestone> = inner.list()
}

fun newCachedDutyPeriodMilestoneStore(inner: DutyPeriodMilestoneStore, ttl: Int, clock: Clock): DutyPeriodMilestoneStore {
    return CachedDutyPeriodMilestoneStore(
        inner = inner,
        cache = newTtlCache<DutyPeriodMilestone>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects duty period milestones. Empty fields match all. */
data class DutyPeriodMilestoneQuery(
    var title: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DutyPeriodMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DutyPeriodMilestone>): MutableList<DutyPeriodMilestone> {
        val matched = filter(items) { candidate: DutyPeriodMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDutyPeriodMilestones(store: DutyPeriodMilestoneStore, query: DutyPeriodMilestoneQuery): MutableList<DutyPeriodMilestone> {
    return query.apply(store.list())
}
