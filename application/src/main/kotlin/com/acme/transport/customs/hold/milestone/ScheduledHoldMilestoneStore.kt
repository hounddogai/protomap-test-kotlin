package com.acme.transport.customs.hold.milestone

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps scheduled hold milestones by code. */
interface ScheduledHoldMilestoneStore {
    fun find(code: String): ScheduledHoldMilestone?
    fun save(item: ScheduledHoldMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledHoldMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledHoldMilestoneStore(
    var items: MutableMap<String, ScheduledHoldMilestone>,
    var order: MutableList<String>,
) : ScheduledHoldMilestoneStore {
    override fun find(code: String): ScheduledHoldMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledHoldMilestone) {
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

    override fun list(): MutableList<ScheduledHoldMilestone> {
        val out = mutableListOf<ScheduledHoldMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledHoldMilestoneStore(): ScheduledHoldMilestoneStore {
    return MemoryScheduledHoldMilestoneStore(
        mutableMapOf<String, ScheduledHoldMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledHoldMilestoneStore(
    var inner: ScheduledHoldMilestoneStore,
    var cache: TtlCache<ScheduledHoldMilestone>,
    var hits: Int,
    var misses: Int,
) : ScheduledHoldMilestoneStore {
    override fun find(code: String): ScheduledHoldMilestone? {
        val hit = (cache.get(code) as ScheduledHoldMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledHoldMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledHoldMilestone> = inner.list()
}

fun newCachedScheduledHoldMilestoneStore(inner: ScheduledHoldMilestoneStore, ttl: Int, clock: Clock): ScheduledHoldMilestoneStore {
    return CachedScheduledHoldMilestoneStore(
        inner = inner,
        cache = newTtlCache<ScheduledHoldMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled hold milestones. Empty fields match all. */
data class ScheduledHoldMilestoneQuery(
    var title: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledHoldMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledHoldMilestone>): MutableList<ScheduledHoldMilestone> {
        val matched = filter(items) { candidate: ScheduledHoldMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledHoldMilestones(store: ScheduledHoldMilestoneStore, query: ScheduledHoldMilestoneQuery): MutableList<ScheduledHoldMilestone> {
    return query.apply(store.list())
}
