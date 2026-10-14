package com.acme.transport.lastmile.drop.rule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered drop adjustments by code. */
interface TieredDropAdjustmentStore {
    fun find(code: String): TieredDropAdjustment?
    fun save(item: TieredDropAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDropAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDropAdjustmentStore(
    var items: MutableMap<String, TieredDropAdjustment>,
    var order: MutableList<String>,
) : TieredDropAdjustmentStore {
    override fun find(code: String): TieredDropAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDropAdjustment) {
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

    override fun list(): MutableList<TieredDropAdjustment> {
        val out = mutableListOf<TieredDropAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDropAdjustmentStore(): TieredDropAdjustmentStore {
    return MemoryTieredDropAdjustmentStore(
        mutableMapOf<String, TieredDropAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDropAdjustmentStore(
    var inner: TieredDropAdjustmentStore,
    var cache: TtlCache<TieredDropAdjustment>,
    var hits: Int,
    var misses: Int,
) : TieredDropAdjustmentStore {
    override fun find(code: String): TieredDropAdjustment? {
        val hit = (cache.get(code) as TieredDropAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDropAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDropAdjustment> = inner.list()
}

fun newCachedTieredDropAdjustmentStore(inner: TieredDropAdjustmentStore, ttl: Int, clock: Clock): TieredDropAdjustmentStore {
    return CachedTieredDropAdjustmentStore(
        inner = inner,
        cache = newTtlCache<TieredDropAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered drop adjustments. Empty fields match all. */
data class TieredDropAdjustmentQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDropAdjustment): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredDropAdjustment>): MutableList<TieredDropAdjustment> {
        val matched = filter(items) { candidate: TieredDropAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDropAdjustments(store: TieredDropAdjustmentStore, query: TieredDropAdjustmentQuery): MutableList<TieredDropAdjustment> {
    return query.apply(store.list())
}
