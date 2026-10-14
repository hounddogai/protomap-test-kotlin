package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps batched route offset targets by code. */
interface BatchedRouteOffsetTargetStore {
    fun find(code: String): BatchedRouteOffsetTarget?
    fun save(item: BatchedRouteOffsetTarget)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedRouteOffsetTarget>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedRouteOffsetTargetStore(
    var items: MutableMap<String, BatchedRouteOffsetTarget>,
    var order: MutableList<String>,
) : BatchedRouteOffsetTargetStore {
    override fun find(code: String): BatchedRouteOffsetTarget? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedRouteOffsetTarget) {
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

    override fun list(): MutableList<BatchedRouteOffsetTarget> {
        val out = mutableListOf<BatchedRouteOffsetTarget>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedRouteOffsetTargetStore(): BatchedRouteOffsetTargetStore {
    return MemoryBatchedRouteOffsetTargetStore(
        mutableMapOf<String, BatchedRouteOffsetTarget>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedRouteOffsetTargetStore(
    var inner: BatchedRouteOffsetTargetStore,
    var cache: TtlCache<BatchedRouteOffsetTarget>,
    var hits: Int,
    var misses: Int,
) : BatchedRouteOffsetTargetStore {
    override fun find(code: String): BatchedRouteOffsetTarget? {
        val hit = (cache.get(code) as BatchedRouteOffsetTarget?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedRouteOffsetTarget) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedRouteOffsetTarget> = inner.list()
}

fun newCachedBatchedRouteOffsetTargetStore(inner: BatchedRouteOffsetTargetStore, ttl: Int, clock: Clock): BatchedRouteOffsetTargetStore {
    return CachedBatchedRouteOffsetTargetStore(
        inner = inner,
        cache = newTtlCache<BatchedRouteOffsetTarget>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched route offset targets. Empty fields match all. */
data class BatchedRouteOffsetTargetQuery(
    var label: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedRouteOffsetTarget): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BatchedRouteOffsetTarget>): MutableList<BatchedRouteOffsetTarget> {
        val matched = filter(items) { candidate: BatchedRouteOffsetTarget -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedRouteOffsetTargets(store: BatchedRouteOffsetTargetStore, query: BatchedRouteOffsetTargetQuery): MutableList<BatchedRouteOffsetTarget> {
    return query.apply(store.list())
}
