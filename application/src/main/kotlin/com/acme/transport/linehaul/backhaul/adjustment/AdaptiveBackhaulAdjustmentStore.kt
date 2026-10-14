package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps adaptive backhaul adjustments by code. */
interface AdaptiveBackhaulAdjustmentStore {
    fun find(code: String): AdaptiveBackhaulAdjustment?
    fun save(item: AdaptiveBackhaulAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveBackhaulAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveBackhaulAdjustmentStore(
    var items: MutableMap<String, AdaptiveBackhaulAdjustment>,
    var order: MutableList<String>,
) : AdaptiveBackhaulAdjustmentStore {
    override fun find(code: String): AdaptiveBackhaulAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveBackhaulAdjustment) {
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

    override fun list(): MutableList<AdaptiveBackhaulAdjustment> {
        val out = mutableListOf<AdaptiveBackhaulAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveBackhaulAdjustmentStore(): AdaptiveBackhaulAdjustmentStore {
    return MemoryAdaptiveBackhaulAdjustmentStore(
        mutableMapOf<String, AdaptiveBackhaulAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveBackhaulAdjustmentStore(
    var inner: AdaptiveBackhaulAdjustmentStore,
    var cache: TtlCache<AdaptiveBackhaulAdjustment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveBackhaulAdjustmentStore {
    override fun find(code: String): AdaptiveBackhaulAdjustment? {
        val hit = (cache.get(code) as AdaptiveBackhaulAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveBackhaulAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveBackhaulAdjustment> = inner.list()
}

fun newCachedAdaptiveBackhaulAdjustmentStore(inner: AdaptiveBackhaulAdjustmentStore, ttl: Int, clock: Clock): AdaptiveBackhaulAdjustmentStore {
    return CachedAdaptiveBackhaulAdjustmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveBackhaulAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive backhaul adjustments. Empty fields match all. */
data class AdaptiveBackhaulAdjustmentQuery(
    var label: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveBackhaulAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<AdaptiveBackhaulAdjustment>): MutableList<AdaptiveBackhaulAdjustment> {
        val matched = filter(items) { candidate: AdaptiveBackhaulAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveBackhaulAdjustments(store: AdaptiveBackhaulAdjustmentStore, query: AdaptiveBackhaulAdjustmentQuery): MutableList<AdaptiveBackhaulAdjustment> {
    return query.apply(store.list())
}
