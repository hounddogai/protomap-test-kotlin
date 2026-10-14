package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps batched attempt overrides by code. */
interface BatchedAttemptOverrideStore {
    fun find(code: String): BatchedAttemptOverride?
    fun save(item: BatchedAttemptOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAttemptOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAttemptOverrideStore(
    var items: MutableMap<String, BatchedAttemptOverride>,
    var order: MutableList<String>,
) : BatchedAttemptOverrideStore {
    override fun find(code: String): BatchedAttemptOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAttemptOverride) {
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

    override fun list(): MutableList<BatchedAttemptOverride> {
        val out = mutableListOf<BatchedAttemptOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAttemptOverrideStore(): BatchedAttemptOverrideStore {
    return MemoryBatchedAttemptOverrideStore(
        mutableMapOf<String, BatchedAttemptOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAttemptOverrideStore(
    var inner: BatchedAttemptOverrideStore,
    var cache: TtlCache<BatchedAttemptOverride>,
    var hits: Int,
    var misses: Int,
) : BatchedAttemptOverrideStore {
    override fun find(code: String): BatchedAttemptOverride? {
        val hit = (cache.get(code) as BatchedAttemptOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAttemptOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAttemptOverride> = inner.list()
}

fun newCachedBatchedAttemptOverrideStore(inner: BatchedAttemptOverrideStore, ttl: Int, clock: Clock): BatchedAttemptOverrideStore {
    return CachedBatchedAttemptOverrideStore(
        inner = inner,
        cache = newTtlCache<BatchedAttemptOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched attempt overrides. Empty fields match all. */
data class BatchedAttemptOverrideQuery(
    var depot: String,
    var minBudget: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAttemptOverride): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.budget < minBudget) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedAttemptOverride>): MutableList<BatchedAttemptOverride> {
        val matched = filter(items) { candidate: BatchedAttemptOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAttemptOverrides(store: BatchedAttemptOverrideStore, query: BatchedAttemptOverrideQuery): MutableList<BatchedAttemptOverride> {
    return query.apply(store.list())
}
