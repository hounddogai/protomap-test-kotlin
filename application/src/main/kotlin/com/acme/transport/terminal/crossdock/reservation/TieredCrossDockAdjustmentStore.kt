package com.acme.transport.terminal.crossdock.reservation

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered cross dock adjustments by code. */
interface TieredCrossDockAdjustmentStore {
    fun find(code: String): TieredCrossDockAdjustment?
    fun save(item: TieredCrossDockAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCrossDockAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCrossDockAdjustmentStore(
    var items: MutableMap<String, TieredCrossDockAdjustment>,
    var order: MutableList<String>,
) : TieredCrossDockAdjustmentStore {
    override fun find(code: String): TieredCrossDockAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCrossDockAdjustment) {
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

    override fun list(): MutableList<TieredCrossDockAdjustment> {
        val out = mutableListOf<TieredCrossDockAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCrossDockAdjustmentStore(): TieredCrossDockAdjustmentStore {
    return MemoryTieredCrossDockAdjustmentStore(
        mutableMapOf<String, TieredCrossDockAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCrossDockAdjustmentStore(
    var inner: TieredCrossDockAdjustmentStore,
    var cache: TtlCache<TieredCrossDockAdjustment>,
    var hits: Int,
    var misses: Int,
) : TieredCrossDockAdjustmentStore {
    override fun find(code: String): TieredCrossDockAdjustment? {
        val hit = (cache.get(code) as TieredCrossDockAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCrossDockAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCrossDockAdjustment> = inner.list()
}

fun newCachedTieredCrossDockAdjustmentStore(inner: TieredCrossDockAdjustmentStore, ttl: Int, clock: Clock): TieredCrossDockAdjustmentStore {
    return CachedTieredCrossDockAdjustmentStore(
        inner = inner,
        cache = newTtlCache<TieredCrossDockAdjustment>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered cross dock adjustments. Empty fields match all. */
data class TieredCrossDockAdjustmentQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCrossDockAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCrossDockAdjustment>): MutableList<TieredCrossDockAdjustment> {
        val matched = filter(items) { candidate: TieredCrossDockAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCrossDockAdjustments(store: TieredCrossDockAdjustmentStore, query: TieredCrossDockAdjustmentQuery): MutableList<TieredCrossDockAdjustment> {
    return query.apply(store.list())
}
