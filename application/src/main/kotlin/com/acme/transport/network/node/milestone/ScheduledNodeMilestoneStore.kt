package com.acme.transport.network.node.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps scheduled node milestones by code. */
interface ScheduledNodeMilestoneStore {
    fun find(code: String): ScheduledNodeMilestone?
    fun save(item: ScheduledNodeMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledNodeMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledNodeMilestoneStore(
    var items: MutableMap<String, ScheduledNodeMilestone>,
    var order: MutableList<String>,
) : ScheduledNodeMilestoneStore {
    override fun find(code: String): ScheduledNodeMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledNodeMilestone) {
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

    override fun list(): MutableList<ScheduledNodeMilestone> {
        val out = mutableListOf<ScheduledNodeMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledNodeMilestoneStore(): ScheduledNodeMilestoneStore {
    return MemoryScheduledNodeMilestoneStore(
        mutableMapOf<String, ScheduledNodeMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledNodeMilestoneStore(
    var inner: ScheduledNodeMilestoneStore,
    var cache: TtlCache<ScheduledNodeMilestone>,
    var hits: Int,
    var misses: Int,
) : ScheduledNodeMilestoneStore {
    override fun find(code: String): ScheduledNodeMilestone? {
        val hit = (cache.get(code) as ScheduledNodeMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledNodeMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledNodeMilestone> = inner.list()
}

fun newCachedScheduledNodeMilestoneStore(inner: ScheduledNodeMilestoneStore, ttl: Int, clock: Clock): ScheduledNodeMilestoneStore {
    return CachedScheduledNodeMilestoneStore(
        inner = inner,
        cache = newTtlCache<ScheduledNodeMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled node milestones. Empty fields match all. */
data class ScheduledNodeMilestoneQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledNodeMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<ScheduledNodeMilestone>): MutableList<ScheduledNodeMilestone> {
        val matched = filter(items) { candidate: ScheduledNodeMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledNodeMilestones(store: ScheduledNodeMilestoneStore, query: ScheduledNodeMilestoneQuery): MutableList<ScheduledNodeMilestone> {
    return query.apply(store.list())
}
