package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps batched backhaul targets by code. */
interface BatchedBackhaulTargetStore {
    fun find(code: String): BatchedBackhaulTarget?
    fun save(item: BatchedBackhaulTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedBackhaulTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedBackhaulTargetStore(
    var items: MutableMap<String, BatchedBackhaulTarget>,
    var order: MutableList<String>,
) : BatchedBackhaulTargetStore {
    override fun find(code: String): BatchedBackhaulTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedBackhaulTarget) {
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

    override fun list(): MutableList<BatchedBackhaulTarget> {
        val out = mutableListOf<BatchedBackhaulTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedBackhaulTargetStore(): BatchedBackhaulTargetStore {
    return MemoryBatchedBackhaulTargetStore(
        mutableMapOf<String, BatchedBackhaulTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedBackhaulTargetStore(
    var inner: BatchedBackhaulTargetStore,
    var cache: TtlCache<BatchedBackhaulTarget>,
    var hits: Int,
    var misses: Int,
) : BatchedBackhaulTargetStore {
    override fun find(code: String): BatchedBackhaulTarget? {
        val hit = (cache.get(code) as BatchedBackhaulTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedBackhaulTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedBackhaulTarget> = inner.list()
}

fun newCachedBatchedBackhaulTargetStore(inner: BatchedBackhaulTargetStore, ttl: Int, clock: Clock): BatchedBackhaulTargetStore {
    return CachedBatchedBackhaulTargetStore(
        inner = inner,
        cache = newTtlCache<BatchedBackhaulTarget>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched backhaul targets. Empty fields match all. */
data class BatchedBackhaulTargetQuery(
    var lane: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedBackhaulTarget): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<BatchedBackhaulTarget>): MutableList<BatchedBackhaulTarget> {
        val matched = filter(items) { candidate: BatchedBackhaulTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedBackhaulTargets(store: BatchedBackhaulTargetStore, query: BatchedBackhaulTargetQuery): MutableList<BatchedBackhaulTarget> {
    return query.apply(store.list())
}
