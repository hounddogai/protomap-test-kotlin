package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched scorecard milestones by code. */
interface BatchedScorecardMilestoneStore {
    fun find(code: String): BatchedScorecardMilestone?
    fun save(item: BatchedScorecardMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedScorecardMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedScorecardMilestoneStore(
    var items: MutableMap<String, BatchedScorecardMilestone>,
    var order: MutableList<String>,
) : BatchedScorecardMilestoneStore {
    override fun find(code: String): BatchedScorecardMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedScorecardMilestone) {
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

    override fun list(): MutableList<BatchedScorecardMilestone> {
        val out = mutableListOf<BatchedScorecardMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedScorecardMilestoneStore(): BatchedScorecardMilestoneStore {
    return MemoryBatchedScorecardMilestoneStore(
        mutableMapOf<String, BatchedScorecardMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedScorecardMilestoneStore(
    var inner: BatchedScorecardMilestoneStore,
    var cache: TtlCache<BatchedScorecardMilestone>,
    var hits: Int,
    var misses: Int,
) : BatchedScorecardMilestoneStore {
    override fun find(code: String): BatchedScorecardMilestone? {
        val hit = (cache.get(code) as BatchedScorecardMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedScorecardMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedScorecardMilestone> = inner.list()
}

fun newCachedBatchedScorecardMilestoneStore(inner: BatchedScorecardMilestoneStore, ttl: Int, clock: Clock): BatchedScorecardMilestoneStore {
    return CachedBatchedScorecardMilestoneStore(
        inner = inner,
        cache = newTtlCache<BatchedScorecardMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched scorecard milestones. Empty fields match all. */
data class BatchedScorecardMilestoneQuery(
    var locale: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedScorecardMilestone): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedScorecardMilestone>): MutableList<BatchedScorecardMilestone> {
        val matched = filter(items) { candidate: BatchedScorecardMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedScorecardMilestones(store: BatchedScorecardMilestoneStore, query: BatchedScorecardMilestoneQuery): MutableList<BatchedScorecardMilestone> {
    return query.apply(store.list())
}
