package com.acme.transport.safety.scorecard.scorecardthreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps batched scorecard rollups by code. */
interface BatchedScorecardRollupStore {
    fun find(code: String): BatchedScorecardRollup?
    fun save(item: BatchedScorecardRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedScorecardRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedScorecardRollupStore(
    var items: MutableMap<String, BatchedScorecardRollup>,
    var order: MutableList<String>,
) : BatchedScorecardRollupStore {
    override fun find(code: String): BatchedScorecardRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedScorecardRollup) {
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

    override fun list(): MutableList<BatchedScorecardRollup> {
        val out = mutableListOf<BatchedScorecardRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedScorecardRollupStore(): BatchedScorecardRollupStore {
    return MemoryBatchedScorecardRollupStore(
        mutableMapOf<String, BatchedScorecardRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedScorecardRollupStore(
    var inner: BatchedScorecardRollupStore,
    var cache: TtlCache<BatchedScorecardRollup>,
    var hits: Int,
    var misses: Int,
) : BatchedScorecardRollupStore {
    override fun find(code: String): BatchedScorecardRollup? {
        val hit = (cache.get(code) as BatchedScorecardRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedScorecardRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedScorecardRollup> = inner.list()
}

fun newCachedBatchedScorecardRollupStore(inner: BatchedScorecardRollupStore, ttl: Int, clock: Clock): BatchedScorecardRollupStore {
    return CachedBatchedScorecardRollupStore(
        inner = inner,
        cache = newTtlCache<BatchedScorecardRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched scorecard rollups. Empty fields match all. */
data class BatchedScorecardRollupQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedScorecardRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedScorecardRollup>): MutableList<BatchedScorecardRollup> {
        val matched = filter(items) { candidate: BatchedScorecardRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedScorecardRollups(store: BatchedScorecardRollupStore, query: BatchedScorecardRollupQuery): MutableList<BatchedScorecardRollup> {
    return query.apply(store.list())
}
