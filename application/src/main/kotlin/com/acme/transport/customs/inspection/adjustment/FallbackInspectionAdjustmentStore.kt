package com.acme.transport.customs.inspection.adjustment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback inspection adjustments by code. */
interface FallbackInspectionAdjustmentStore {
    fun find(code: String): FallbackInspectionAdjustment?
    fun save(item: FallbackInspectionAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackInspectionAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackInspectionAdjustmentStore(
    var items: MutableMap<String, FallbackInspectionAdjustment>,
    var order: MutableList<String>,
) : FallbackInspectionAdjustmentStore {
    override fun find(code: String): FallbackInspectionAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackInspectionAdjustment) {
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

    override fun list(): MutableList<FallbackInspectionAdjustment> {
        val out = mutableListOf<FallbackInspectionAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackInspectionAdjustmentStore(): FallbackInspectionAdjustmentStore {
    return MemoryFallbackInspectionAdjustmentStore(
        mutableMapOf<String, FallbackInspectionAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackInspectionAdjustmentStore(
    var inner: FallbackInspectionAdjustmentStore,
    var cache: TtlCache<FallbackInspectionAdjustment>,
    var hits: Int,
    var misses: Int,
) : FallbackInspectionAdjustmentStore {
    override fun find(code: String): FallbackInspectionAdjustment? {
        val hit = (cache.get(code) as FallbackInspectionAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackInspectionAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackInspectionAdjustment> = inner.list()
}

fun newCachedFallbackInspectionAdjustmentStore(inner: FallbackInspectionAdjustmentStore, ttl: Int, clock: Clock): FallbackInspectionAdjustmentStore {
    return CachedFallbackInspectionAdjustmentStore(
        inner = inner,
        cache = newTtlCache<FallbackInspectionAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback inspection adjustments. Empty fields match all. */
data class FallbackInspectionAdjustmentQuery(
    var segment: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackInspectionAdjustment): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackInspectionAdjustment>): MutableList<FallbackInspectionAdjustment> {
        val matched = filter(items) { candidate: FallbackInspectionAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackInspectionAdjustments(store: FallbackInspectionAdjustmentStore, query: FallbackInspectionAdjustmentQuery): MutableList<FallbackInspectionAdjustment> {
    return query.apply(store.list())
}
