package com.acme.transport.lastmile.attempt.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched attempt milestones by code. */
interface BatchedAttemptMilestoneStore {
    fun find(code: String): BatchedAttemptMilestone?
    fun save(item: BatchedAttemptMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAttemptMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAttemptMilestoneStore(
    var items: MutableMap<String, BatchedAttemptMilestone>,
    var order: MutableList<String>,
) : BatchedAttemptMilestoneStore {
    override fun find(code: String): BatchedAttemptMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAttemptMilestone) {
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

    override fun list(): MutableList<BatchedAttemptMilestone> {
        val out = mutableListOf<BatchedAttemptMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAttemptMilestoneStore(): BatchedAttemptMilestoneStore {
    return MemoryBatchedAttemptMilestoneStore(
        mutableMapOf<String, BatchedAttemptMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAttemptMilestoneStore(
    var inner: BatchedAttemptMilestoneStore,
    var cache: TtlCache<BatchedAttemptMilestone>,
    var hits: Int,
    var misses: Int,
) : BatchedAttemptMilestoneStore {
    override fun find(code: String): BatchedAttemptMilestone? {
        val hit = (cache.get(code) as BatchedAttemptMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAttemptMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAttemptMilestone> = inner.list()
}

fun newCachedBatchedAttemptMilestoneStore(inner: BatchedAttemptMilestoneStore, ttl: Int, clock: Clock): BatchedAttemptMilestoneStore {
    return CachedBatchedAttemptMilestoneStore(
        inner = inner,
        cache = newTtlCache<BatchedAttemptMilestone>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched attempt milestones. Empty fields match all. */
data class BatchedAttemptMilestoneQuery(
    var name: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAttemptMilestone): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAttemptMilestone>): MutableList<BatchedAttemptMilestone> {
        val matched = filter(items) { candidate: BatchedAttemptMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAttemptMilestones(store: BatchedAttemptMilestoneStore, query: BatchedAttemptMilestoneQuery): MutableList<BatchedAttemptMilestone> {
    return query.apply(store.list())
}
