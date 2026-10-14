package com.acme.transport.safety.scorecard.scorecardwindow

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched scorecard thresholds by code. */
interface BatchedScorecardThresholdStore {
    fun find(code: String): BatchedScorecardThreshold?
    fun save(item: BatchedScorecardThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedScorecardThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedScorecardThresholdStore(
    var items: MutableMap<String, BatchedScorecardThreshold>,
    var order: MutableList<String>,
) : BatchedScorecardThresholdStore {
    override fun find(code: String): BatchedScorecardThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedScorecardThreshold) {
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

    override fun list(): MutableList<BatchedScorecardThreshold> {
        val out = mutableListOf<BatchedScorecardThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedScorecardThresholdStore(): BatchedScorecardThresholdStore {
    return MemoryBatchedScorecardThresholdStore(
        mutableMapOf<String, BatchedScorecardThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedScorecardThresholdStore(
    var inner: BatchedScorecardThresholdStore,
    var cache: TtlCache<BatchedScorecardThreshold>,
    var hits: Int,
    var misses: Int,
) : BatchedScorecardThresholdStore {
    override fun find(code: String): BatchedScorecardThreshold? {
        val hit = (cache.get(code) as BatchedScorecardThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedScorecardThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedScorecardThreshold> = inner.list()
}

fun newCachedBatchedScorecardThresholdStore(inner: BatchedScorecardThresholdStore, ttl: Int, clock: Clock): BatchedScorecardThresholdStore {
    return CachedBatchedScorecardThresholdStore(
        inner = inner,
        cache = newTtlCache<BatchedScorecardThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched scorecard thresholds. Empty fields match all. */
data class BatchedScorecardThresholdQuery(
    var source: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedScorecardThreshold): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedScorecardThreshold>): MutableList<BatchedScorecardThreshold> {
        val matched = filter(items) { candidate: BatchedScorecardThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedScorecardThresholds(store: BatchedScorecardThresholdStore, query: BatchedScorecardThresholdQuery): MutableList<BatchedScorecardThreshold> {
    return query.apply(store.list())
}
