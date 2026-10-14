package com.acme.transport.freightpay.ratecard.forecast

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched rate card milestones by code. */
interface BatchedRateCardMilestoneStore {
    fun find(code: String): BatchedRateCardMilestone?
    fun save(item: BatchedRateCardMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRateCardMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRateCardMilestoneStore(
    var items: MutableMap<String, BatchedRateCardMilestone>,
    var order: MutableList<String>,
) : BatchedRateCardMilestoneStore {
    override fun find(code: String): BatchedRateCardMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRateCardMilestone) {
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

    override fun list(): MutableList<BatchedRateCardMilestone> {
        val out = mutableListOf<BatchedRateCardMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRateCardMilestoneStore(): BatchedRateCardMilestoneStore {
    return MemoryBatchedRateCardMilestoneStore(
        mutableMapOf<String, BatchedRateCardMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRateCardMilestoneStore(
    var inner: BatchedRateCardMilestoneStore,
    var cache: TtlCache<BatchedRateCardMilestone>,
    var hits: Int,
    var misses: Int,
) : BatchedRateCardMilestoneStore {
    override fun find(code: String): BatchedRateCardMilestone? {
        val hit = (cache.get(code) as BatchedRateCardMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRateCardMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRateCardMilestone> = inner.list()
}

fun newCachedBatchedRateCardMilestoneStore(inner: BatchedRateCardMilestoneStore, ttl: Int, clock: Clock): BatchedRateCardMilestoneStore {
    return CachedBatchedRateCardMilestoneStore(
        inner = inner,
        cache = newTtlCache<BatchedRateCardMilestone>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched rate card milestones. Empty fields match all. */
data class BatchedRateCardMilestoneQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRateCardMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRateCardMilestone>): MutableList<BatchedRateCardMilestone> {
        val matched = filter(items) { candidate: BatchedRateCardMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRateCardMilestones(store: BatchedRateCardMilestoneStore, query: BatchedRateCardMilestoneQuery): MutableList<BatchedRateCardMilestone> {
    return query.apply(store.list())
}
