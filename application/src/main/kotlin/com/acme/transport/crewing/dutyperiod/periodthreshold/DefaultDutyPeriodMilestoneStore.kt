package com.acme.transport.crewing.dutyperiod.periodthreshold

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default duty period milestones by code. */
interface DefaultDutyPeriodMilestoneStore {
    fun find(code: String): DefaultDutyPeriodMilestone?
    fun save(item: DefaultDutyPeriodMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDutyPeriodMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDutyPeriodMilestoneStore(
    var items: MutableMap<String, DefaultDutyPeriodMilestone>,
    var order: MutableList<String>,
) : DefaultDutyPeriodMilestoneStore {
    override fun find(code: String): DefaultDutyPeriodMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDutyPeriodMilestone) {
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

    override fun list(): MutableList<DefaultDutyPeriodMilestone> {
        val out = mutableListOf<DefaultDutyPeriodMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDutyPeriodMilestoneStore(): DefaultDutyPeriodMilestoneStore {
    return MemoryDefaultDutyPeriodMilestoneStore(
        mutableMapOf<String, DefaultDutyPeriodMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDutyPeriodMilestoneStore(
    var inner: DefaultDutyPeriodMilestoneStore,
    var cache: TtlCache<DefaultDutyPeriodMilestone>,
    var hits: Int,
    var misses: Int,
) : DefaultDutyPeriodMilestoneStore {
    override fun find(code: String): DefaultDutyPeriodMilestone? {
        val hit = (cache.get(code) as DefaultDutyPeriodMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDutyPeriodMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDutyPeriodMilestone> = inner.list()
}

fun newCachedDefaultDutyPeriodMilestoneStore(inner: DefaultDutyPeriodMilestoneStore, ttl: Int, clock: Clock): DefaultDutyPeriodMilestoneStore {
    return CachedDefaultDutyPeriodMilestoneStore(
        inner = inner,
        cache = newTtlCache<DefaultDutyPeriodMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default duty period milestones. Empty fields match all. */
data class DefaultDutyPeriodMilestoneQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDutyPeriodMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDutyPeriodMilestone>): MutableList<DefaultDutyPeriodMilestone> {
        val matched = filter(items) { candidate: DefaultDutyPeriodMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDutyPeriodMilestones(store: DefaultDutyPeriodMilestoneStore, query: DefaultDutyPeriodMilestoneQuery): MutableList<DefaultDutyPeriodMilestone> {
    return query.apply(store.list())
}
