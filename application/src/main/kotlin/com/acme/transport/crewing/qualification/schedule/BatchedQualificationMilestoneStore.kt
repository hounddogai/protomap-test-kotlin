package com.acme.transport.crewing.qualification.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched qualification milestones by code. */
interface BatchedQualificationMilestoneStore {
    fun find(code: String): BatchedQualificationMilestone?
    fun save(item: BatchedQualificationMilestone)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedQualificationMilestone>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedQualificationMilestoneStore(
    var items: MutableMap<String, BatchedQualificationMilestone>,
    var order: MutableList<String>,
) : BatchedQualificationMilestoneStore {
    override fun find(code: String): BatchedQualificationMilestone? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedQualificationMilestone) {
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

    override fun list(): MutableList<BatchedQualificationMilestone> {
        val out = mutableListOf<BatchedQualificationMilestone>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedQualificationMilestoneStore(): BatchedQualificationMilestoneStore {
    return MemoryBatchedQualificationMilestoneStore(
        mutableMapOf<String, BatchedQualificationMilestone>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedQualificationMilestoneStore(
    var inner: BatchedQualificationMilestoneStore,
    var cache: TtlCache<BatchedQualificationMilestone>,
    var hits: Int,
    var misses: Int,
) : BatchedQualificationMilestoneStore {
    override fun find(code: String): BatchedQualificationMilestone? {
        val hit = (cache.get(code) as BatchedQualificationMilestone?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedQualificationMilestone) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedQualificationMilestone> = inner.list()
}

fun newCachedBatchedQualificationMilestoneStore(inner: BatchedQualificationMilestoneStore, ttl: Int, clock: Clock): BatchedQualificationMilestoneStore {
    return CachedBatchedQualificationMilestoneStore(
        inner = inner,
        cache = newTtlCache<BatchedQualificationMilestone>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched qualification milestones. Empty fields match all. */
data class BatchedQualificationMilestoneQuery(
    var title: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedQualificationMilestone): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedQualificationMilestone>): MutableList<BatchedQualificationMilestone> {
        val matched = filter(items) { candidate: BatchedQualificationMilestone -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedQualificationMilestones(store: BatchedQualificationMilestoneStore, query: BatchedQualificationMilestoneQuery): MutableList<BatchedQualificationMilestone> {
    return query.apply(store.list())
}
