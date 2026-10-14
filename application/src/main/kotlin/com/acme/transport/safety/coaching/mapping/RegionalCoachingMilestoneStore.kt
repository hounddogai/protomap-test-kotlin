package com.acme.transport.safety.coaching.mapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps regional coaching milestones by code. */
interface RegionalCoachingMilestoneStore {
    fun find(code: String): RegionalCoachingMilestone?
    fun save(item: RegionalCoachingMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalCoachingMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalCoachingMilestoneStore(
    var items: MutableMap<String, RegionalCoachingMilestone>,
    var order: MutableList<String>,
) : RegionalCoachingMilestoneStore {
    override fun find(code: String): RegionalCoachingMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalCoachingMilestone) {
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

    override fun list(): MutableList<RegionalCoachingMilestone> {
        val out = mutableListOf<RegionalCoachingMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalCoachingMilestoneStore(): RegionalCoachingMilestoneStore {
    return MemoryRegionalCoachingMilestoneStore(
        mutableMapOf<String, RegionalCoachingMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalCoachingMilestoneStore(
    var inner: RegionalCoachingMilestoneStore,
    var cache: TtlCache<RegionalCoachingMilestone>,
    var hits: Int,
    var misses: Int,
) : RegionalCoachingMilestoneStore {
    override fun find(code: String): RegionalCoachingMilestone? {
        val hit = (cache.get(code) as RegionalCoachingMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalCoachingMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalCoachingMilestone> = inner.list()
}

fun newCachedRegionalCoachingMilestoneStore(inner: RegionalCoachingMilestoneStore, ttl: Int, clock: Clock): RegionalCoachingMilestoneStore {
    return CachedRegionalCoachingMilestoneStore(
        inner = inner,
        cache = newTtlCache<RegionalCoachingMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional coaching milestones. Empty fields match all. */
data class RegionalCoachingMilestoneQuery(
    var channel: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalCoachingMilestone): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalCoachingMilestone>): MutableList<RegionalCoachingMilestone> {
        val matched = filter(items) { candidate: RegionalCoachingMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalCoachingMilestones(store: RegionalCoachingMilestoneStore, query: RegionalCoachingMilestoneQuery): MutableList<RegionalCoachingMilestone> {
    return query.apply(store.list())
}
