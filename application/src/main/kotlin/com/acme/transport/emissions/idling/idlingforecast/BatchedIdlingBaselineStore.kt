package com.acme.transport.emissions.idling.idlingforecast

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched idling baselines by code. */
interface BatchedIdlingBaselineStore {
    fun find(code: String): BatchedIdlingBaseline?
    fun save(item: BatchedIdlingBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedIdlingBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedIdlingBaselineStore(
    var items: MutableMap<String, BatchedIdlingBaseline>,
    var order: MutableList<String>,
) : BatchedIdlingBaselineStore {
    override fun find(code: String): BatchedIdlingBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedIdlingBaseline) {
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

    override fun list(): MutableList<BatchedIdlingBaseline> {
        val out = mutableListOf<BatchedIdlingBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedIdlingBaselineStore(): BatchedIdlingBaselineStore {
    return MemoryBatchedIdlingBaselineStore(
        mutableMapOf<String, BatchedIdlingBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedIdlingBaselineStore(
    var inner: BatchedIdlingBaselineStore,
    var cache: TtlCache<BatchedIdlingBaseline>,
    var hits: Int,
    var misses: Int,
) : BatchedIdlingBaselineStore {
    override fun find(code: String): BatchedIdlingBaseline? {
        val hit = (cache.get(code) as BatchedIdlingBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedIdlingBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedIdlingBaseline> = inner.list()
}

fun newCachedBatchedIdlingBaselineStore(inner: BatchedIdlingBaselineStore, ttl: Int, clock: Clock): BatchedIdlingBaselineStore {
    return CachedBatchedIdlingBaselineStore(
        inner = inner,
        cache = newTtlCache<BatchedIdlingBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched idling baselines. Empty fields match all. */
data class BatchedIdlingBaselineQuery(
    var lane: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedIdlingBaseline): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedIdlingBaseline>): MutableList<BatchedIdlingBaseline> {
        val matched = filter(items) { candidate: BatchedIdlingBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedIdlingBaselines(store: BatchedIdlingBaselineStore, query: BatchedIdlingBaselineQuery): MutableList<BatchedIdlingBaseline> {
    return query.apply(store.list())
}
