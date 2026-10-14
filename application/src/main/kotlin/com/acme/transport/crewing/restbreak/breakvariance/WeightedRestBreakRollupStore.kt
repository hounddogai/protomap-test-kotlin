package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted rest break rollups by code. */
interface WeightedRestBreakRollupStore {
    fun find(code: String): WeightedRestBreakRollup?
    fun save(item: WeightedRestBreakRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRestBreakRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRestBreakRollupStore(
    var items: MutableMap<String, WeightedRestBreakRollup>,
    var order: MutableList<String>,
) : WeightedRestBreakRollupStore {
    override fun find(code: String): WeightedRestBreakRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRestBreakRollup) {
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

    override fun list(): MutableList<WeightedRestBreakRollup> {
        val out = mutableListOf<WeightedRestBreakRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRestBreakRollupStore(): WeightedRestBreakRollupStore {
    return MemoryWeightedRestBreakRollupStore(
        mutableMapOf<String, WeightedRestBreakRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRestBreakRollupStore(
    var inner: WeightedRestBreakRollupStore,
    var cache: TtlCache<WeightedRestBreakRollup>,
    var hits: Int,
    var misses: Int,
) : WeightedRestBreakRollupStore {
    override fun find(code: String): WeightedRestBreakRollup? {
        val hit = (cache.get(code) as WeightedRestBreakRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRestBreakRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRestBreakRollup> = inner.list()
}

fun newCachedWeightedRestBreakRollupStore(inner: WeightedRestBreakRollupStore, ttl: Int, clock: Clock): WeightedRestBreakRollupStore {
    return CachedWeightedRestBreakRollupStore(
        inner = inner,
        cache = newTtlCache<WeightedRestBreakRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted rest break rollups. Empty fields match all. */
data class WeightedRestBreakRollupQuery(
    var category: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRestBreakRollup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedRestBreakRollup>): MutableList<WeightedRestBreakRollup> {
        val matched = filter(items) { candidate: WeightedRestBreakRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRestBreakRollups(store: WeightedRestBreakRollupStore, query: WeightedRestBreakRollupQuery): MutableList<WeightedRestBreakRollup> {
    return query.apply(store.list())
}
