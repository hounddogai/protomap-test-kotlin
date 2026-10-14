package com.acme.transport.terminal.crossdock.docknotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps adaptive cross dock adjustments by code. */
interface AdaptiveCrossDockAdjustmentStore {
    fun find(code: String): AdaptiveCrossDockAdjustment?
    fun save(item: AdaptiveCrossDockAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCrossDockAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCrossDockAdjustmentStore(
    var items: MutableMap<String, AdaptiveCrossDockAdjustment>,
    var order: MutableList<String>,
) : AdaptiveCrossDockAdjustmentStore {
    override fun find(code: String): AdaptiveCrossDockAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCrossDockAdjustment) {
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

    override fun list(): MutableList<AdaptiveCrossDockAdjustment> {
        val out = mutableListOf<AdaptiveCrossDockAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCrossDockAdjustmentStore(): AdaptiveCrossDockAdjustmentStore {
    return MemoryAdaptiveCrossDockAdjustmentStore(
        mutableMapOf<String, AdaptiveCrossDockAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCrossDockAdjustmentStore(
    var inner: AdaptiveCrossDockAdjustmentStore,
    var cache: TtlCache<AdaptiveCrossDockAdjustment>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCrossDockAdjustmentStore {
    override fun find(code: String): AdaptiveCrossDockAdjustment? {
        val hit = (cache.get(code) as AdaptiveCrossDockAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCrossDockAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCrossDockAdjustment> = inner.list()
}

fun newCachedAdaptiveCrossDockAdjustmentStore(inner: AdaptiveCrossDockAdjustmentStore, ttl: Int, clock: Clock): AdaptiveCrossDockAdjustmentStore {
    return CachedAdaptiveCrossDockAdjustmentStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCrossDockAdjustment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive cross dock adjustments. Empty fields match all. */
data class AdaptiveCrossDockAdjustmentQuery(
    var country: String,
    var minPriority: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCrossDockAdjustment): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.priority < minPriority) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveCrossDockAdjustment>): MutableList<AdaptiveCrossDockAdjustment> {
        val matched = filter(items) { candidate: AdaptiveCrossDockAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCrossDockAdjustments(store: AdaptiveCrossDockAdjustmentStore, query: AdaptiveCrossDockAdjustmentQuery): MutableList<AdaptiveCrossDockAdjustment> {
    return query.apply(store.list())
}
