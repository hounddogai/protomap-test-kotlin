package com.acme.transport.safety.incident.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched incident milestones by code. */
interface BatchedIncidentMilestoneStore {
    fun find(code: String): BatchedIncidentMilestone?
    fun save(item: BatchedIncidentMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedIncidentMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedIncidentMilestoneStore(
    var items: MutableMap<String, BatchedIncidentMilestone>,
    var order: MutableList<String>,
) : BatchedIncidentMilestoneStore {
    override fun find(code: String): BatchedIncidentMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedIncidentMilestone) {
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

    override fun list(): MutableList<BatchedIncidentMilestone> {
        val out = mutableListOf<BatchedIncidentMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedIncidentMilestoneStore(): BatchedIncidentMilestoneStore {
    return MemoryBatchedIncidentMilestoneStore(
        mutableMapOf<String, BatchedIncidentMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedIncidentMilestoneStore(
    var inner: BatchedIncidentMilestoneStore,
    var cache: TtlCache<BatchedIncidentMilestone>,
    var hits: Int,
    var misses: Int,
) : BatchedIncidentMilestoneStore {
    override fun find(code: String): BatchedIncidentMilestone? {
        val hit = (cache.get(code) as BatchedIncidentMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedIncidentMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedIncidentMilestone> = inner.list()
}

fun newCachedBatchedIncidentMilestoneStore(inner: BatchedIncidentMilestoneStore, ttl: Int, clock: Clock): BatchedIncidentMilestoneStore {
    return CachedBatchedIncidentMilestoneStore(
        inner = inner,
        cache = newTtlCache<BatchedIncidentMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched incident milestones. Empty fields match all. */
data class BatchedIncidentMilestoneQuery(
    var title: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedIncidentMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedIncidentMilestone>): MutableList<BatchedIncidentMilestone> {
        val matched = filter(items) { candidate: BatchedIncidentMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedIncidentMilestones(store: BatchedIncidentMilestoneStore, query: BatchedIncidentMilestoneQuery): MutableList<BatchedIncidentMilestone> {
    return query.apply(store.list())
}
