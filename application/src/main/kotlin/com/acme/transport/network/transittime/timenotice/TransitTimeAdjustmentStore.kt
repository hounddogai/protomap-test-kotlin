package com.acme.transport.network.transittime.timenotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps transit time adjustments by code. */
interface TransitTimeAdjustmentStore {
    fun find(code: String): TransitTimeAdjustment?
    fun save(item: TransitTimeAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TransitTimeAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTransitTimeAdjustmentStore(
    var items: MutableMap<String, TransitTimeAdjustment>,
    var order: MutableList<String>,
) : TransitTimeAdjustmentStore {
    override fun find(code: String): TransitTimeAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TransitTimeAdjustment) {
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

    override fun list(): MutableList<TransitTimeAdjustment> {
        val out = mutableListOf<TransitTimeAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTransitTimeAdjustmentStore(): TransitTimeAdjustmentStore {
    return MemoryTransitTimeAdjustmentStore(
        mutableMapOf<String, TransitTimeAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTransitTimeAdjustmentStore(
    var inner: TransitTimeAdjustmentStore,
    var cache: TtlCache<TransitTimeAdjustment>,
    var hits: Int,
    var misses: Int,
) : TransitTimeAdjustmentStore {
    override fun find(code: String): TransitTimeAdjustment? {
        val hit = (cache.get(code) as TransitTimeAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TransitTimeAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TransitTimeAdjustment> = inner.list()
}

fun newCachedTransitTimeAdjustmentStore(inner: TransitTimeAdjustmentStore, ttl: Int, clock: Clock): TransitTimeAdjustmentStore {
    return CachedTransitTimeAdjustmentStore(
        inner = inner,
        cache = newTtlCache<TransitTimeAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects transit time adjustments. Empty fields match all. */
data class TransitTimeAdjustmentQuery(
    var label: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TransitTimeAdjustment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TransitTimeAdjustment>): MutableList<TransitTimeAdjustment> {
        val matched = filter(items) { candidate: TransitTimeAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTransitTimeAdjustments(store: TransitTimeAdjustmentStore, query: TransitTimeAdjustmentQuery): MutableList<TransitTimeAdjustment> {
    return query.apply(store.list())
}
