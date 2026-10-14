package com.acme.transport.network.node.nodesnapshot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps batched node milestones by code. */
interface BatchedNodeMilestoneStore {
    fun find(code: String): BatchedNodeMilestone?
    fun save(item: BatchedNodeMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedNodeMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedNodeMilestoneStore(
    var items: MutableMap<String, BatchedNodeMilestone>,
    var order: MutableList<String>,
) : BatchedNodeMilestoneStore {
    override fun find(code: String): BatchedNodeMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedNodeMilestone) {
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

    override fun list(): MutableList<BatchedNodeMilestone> {
        val out = mutableListOf<BatchedNodeMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedNodeMilestoneStore(): BatchedNodeMilestoneStore {
    return MemoryBatchedNodeMilestoneStore(
        mutableMapOf<String, BatchedNodeMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedNodeMilestoneStore(
    var inner: BatchedNodeMilestoneStore,
    var cache: TtlCache<BatchedNodeMilestone>,
    var hits: Int,
    var misses: Int,
) : BatchedNodeMilestoneStore {
    override fun find(code: String): BatchedNodeMilestone? {
        val hit = (cache.get(code) as BatchedNodeMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedNodeMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedNodeMilestone> = inner.list()
}

fun newCachedBatchedNodeMilestoneStore(inner: BatchedNodeMilestoneStore, ttl: Int, clock: Clock): BatchedNodeMilestoneStore {
    return CachedBatchedNodeMilestoneStore(
        inner = inner,
        cache = newTtlCache<BatchedNodeMilestone>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched node milestones. Empty fields match all. */
data class BatchedNodeMilestoneQuery(
    var lane: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedNodeMilestone): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedNodeMilestone>): MutableList<BatchedNodeMilestone> {
        val matched = filter(items) { candidate: BatchedNodeMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedNodeMilestones(store: BatchedNodeMilestoneStore, query: BatchedNodeMilestoneQuery): MutableList<BatchedNodeMilestone> {
    return query.apply(store.list())
}
