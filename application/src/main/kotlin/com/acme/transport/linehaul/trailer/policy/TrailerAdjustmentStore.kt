package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer adjustments by code. */
interface TrailerAdjustmentStore {
    fun find(code: String): TrailerAdjustment?
    fun save(item: TrailerAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerAdjustmentStore(
    var items: MutableMap<String, TrailerAdjustment>,
    var order: MutableList<String>,
) : TrailerAdjustmentStore {
    override fun find(code: String): TrailerAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerAdjustment) {
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

    override fun list(): MutableList<TrailerAdjustment> {
        val out = mutableListOf<TrailerAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerAdjustmentStore(): TrailerAdjustmentStore {
    return MemoryTrailerAdjustmentStore(mutableMapOf<String, TrailerAdjustment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerAdjustmentStore(
    var inner: TrailerAdjustmentStore,
    var cache: TtlCache<TrailerAdjustment>,
    var hits: Int,
    var misses: Int,
) : TrailerAdjustmentStore {
    override fun find(code: String): TrailerAdjustment? {
        val hit = (cache.get(code) as TrailerAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerAdjustment> = inner.list()
}

fun newCachedTrailerAdjustmentStore(inner: TrailerAdjustmentStore, ttl: Int, clock: Clock): TrailerAdjustmentStore {
    return CachedTrailerAdjustmentStore(
        inner = inner,
        cache = newTtlCache<TrailerAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer adjustments. Empty fields match all. */
data class TrailerAdjustmentQuery(
    var region: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerAdjustment): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerAdjustment>): MutableList<TrailerAdjustment> {
        val matched = filter(items) { candidate: TrailerAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerAdjustments(store: TrailerAdjustmentStore, query: TrailerAdjustmentQuery): MutableList<TrailerAdjustment> {
    return query.apply(store.list())
}
