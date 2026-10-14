package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched trailer scores by code. */
interface BatchedTrailerScoreStore {
    fun find(code: String): BatchedTrailerScore?
    fun save(item: BatchedTrailerScore)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedTrailerScore>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedTrailerScoreStore(
    var items: MutableMap<String, BatchedTrailerScore>,
    var order: MutableList<String>,
) : BatchedTrailerScoreStore {
    override fun find(code: String): BatchedTrailerScore? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedTrailerScore) {
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

    override fun list(): MutableList<BatchedTrailerScore> {
        val out = mutableListOf<BatchedTrailerScore>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedTrailerScoreStore(): BatchedTrailerScoreStore {
    return MemoryBatchedTrailerScoreStore(mutableMapOf<String, BatchedTrailerScore>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedTrailerScoreStore(
    var inner: BatchedTrailerScoreStore,
    var cache: TtlCache<BatchedTrailerScore>,
    var hits: Int,
    var misses: Int,
) : BatchedTrailerScoreStore {
    override fun find(code: String): BatchedTrailerScore? {
        val hit = (cache.get(code) as BatchedTrailerScore?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedTrailerScore) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedTrailerScore> = inner.list()
}

fun newCachedBatchedTrailerScoreStore(inner: BatchedTrailerScoreStore, ttl: Int, clock: Clock): BatchedTrailerScoreStore {
    return CachedBatchedTrailerScoreStore(
        inner = inner,
        cache = newTtlCache<BatchedTrailerScore>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched trailer scores. Empty fields match all. */
data class BatchedTrailerScoreQuery(
    var label: String,
    var minCost: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedTrailerScore): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.cost < minCost) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedTrailerScore>): MutableList<BatchedTrailerScore> {
        val matched = filter(items) { candidate: BatchedTrailerScore -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedTrailerScores(store: BatchedTrailerScoreStore, query: BatchedTrailerScoreQuery): MutableList<BatchedTrailerScore> {
    return query.apply(store.list())
}
