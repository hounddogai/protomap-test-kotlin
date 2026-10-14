package com.acme.transport.crewing.restbreak.breakmilestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled rest break milestones by code. */
interface ScheduledRestBreakMilestoneStore {
    fun find(code: String): ScheduledRestBreakMilestone?
    fun save(item: ScheduledRestBreakMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledRestBreakMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledRestBreakMilestoneStore(
    var items: MutableMap<String, ScheduledRestBreakMilestone>,
    var order: MutableList<String>,
) : ScheduledRestBreakMilestoneStore {
    override fun find(code: String): ScheduledRestBreakMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledRestBreakMilestone) {
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

    override fun list(): MutableList<ScheduledRestBreakMilestone> {
        val out = mutableListOf<ScheduledRestBreakMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledRestBreakMilestoneStore(): ScheduledRestBreakMilestoneStore {
    return MemoryScheduledRestBreakMilestoneStore(
        mutableMapOf<String, ScheduledRestBreakMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledRestBreakMilestoneStore(
    var inner: ScheduledRestBreakMilestoneStore,
    var cache: TtlCache<ScheduledRestBreakMilestone>,
    var hits: Int,
    var misses: Int,
) : ScheduledRestBreakMilestoneStore {
    override fun find(code: String): ScheduledRestBreakMilestone? {
        val hit = (cache.get(code) as ScheduledRestBreakMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledRestBreakMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledRestBreakMilestone> = inner.list()
}

fun newCachedScheduledRestBreakMilestoneStore(inner: ScheduledRestBreakMilestoneStore, ttl: Int, clock: Clock): ScheduledRestBreakMilestoneStore {
    return CachedScheduledRestBreakMilestoneStore(
        inner = inner,
        cache = newTtlCache<ScheduledRestBreakMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled rest break milestones. Empty fields match all. */
data class ScheduledRestBreakMilestoneQuery(
    var region: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledRestBreakMilestone): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledRestBreakMilestone>): MutableList<ScheduledRestBreakMilestone> {
        val matched = filter(items) { candidate: ScheduledRestBreakMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledRestBreakMilestones(store: ScheduledRestBreakMilestoneStore, query: ScheduledRestBreakMilestoneQuery): MutableList<ScheduledRestBreakMilestone> {
    return query.apply(store.list())
}
