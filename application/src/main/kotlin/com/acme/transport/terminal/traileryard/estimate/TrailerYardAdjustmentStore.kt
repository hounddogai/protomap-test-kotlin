package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard adjustments by code. */
interface TrailerYardAdjustmentStore {
    fun find(code: String): TrailerYardAdjustment?
    fun save(item: TrailerYardAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardAdjustmentStore(
    var items: MutableMap<String, TrailerYardAdjustment>,
    var order: MutableList<String>,
) : TrailerYardAdjustmentStore {
    override fun find(code: String): TrailerYardAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardAdjustment) {
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

    override fun list(): MutableList<TrailerYardAdjustment> {
        val out = mutableListOf<TrailerYardAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardAdjustmentStore(): TrailerYardAdjustmentStore {
    return MemoryTrailerYardAdjustmentStore(
        mutableMapOf<String, TrailerYardAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardAdjustmentStore(
    var inner: TrailerYardAdjustmentStore,
    var cache: TtlCache<TrailerYardAdjustment>,
    var hits: Int,
    var misses: Int,
) : TrailerYardAdjustmentStore {
    override fun find(code: String): TrailerYardAdjustment? {
        val hit = (cache.get(code) as TrailerYardAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardAdjustment> = inner.list()
}

fun newCachedTrailerYardAdjustmentStore(inner: TrailerYardAdjustmentStore, ttl: Int, clock: Clock): TrailerYardAdjustmentStore {
    return CachedTrailerYardAdjustmentStore(
        inner = inner,
        cache = newTtlCache<TrailerYardAdjustment>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard adjustments. Empty fields match all. */
data class TrailerYardAdjustmentQuery(
    var name: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardAdjustment>): MutableList<TrailerYardAdjustment> {
        val matched = filter(items) { candidate: TrailerYardAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardAdjustments(store: TrailerYardAdjustmentStore, query: TrailerYardAdjustmentQuery): MutableList<TrailerYardAdjustment> {
    return query.apply(store.list())
}
