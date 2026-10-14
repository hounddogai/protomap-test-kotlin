package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire rollups by code. */
interface TireRollupStore {
    fun find(code: String): TireRollup?
    fun save(item: TireRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireRollupStore(
    var items: MutableMap<String, TireRollup>,
    var order: MutableList<String>,
) : TireRollupStore {
    override fun find(code: String): TireRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireRollup) {
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

    override fun list(): MutableList<TireRollup> {
        val out = mutableListOf<TireRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireRollupStore(): TireRollupStore {
    return MemoryTireRollupStore(mutableMapOf<String, TireRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireRollupStore(
    var inner: TireRollupStore,
    var cache: TtlCache<TireRollup>,
    var hits: Int,
    var misses: Int,
) : TireRollupStore {
    override fun find(code: String): TireRollup? {
        val hit = (cache.get(code) as TireRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireRollup> = inner.list()
}

fun newCachedTireRollupStore(inner: TireRollupStore, ttl: Int, clock: Clock): TireRollupStore {
    return CachedTireRollupStore(
        inner = inner,
        cache = newTtlCache<TireRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire rollups. Empty fields match all. */
data class TireRollupQuery(
    var category: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireRollup): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireRollup>): MutableList<TireRollup> {
        val matched = filter(items) { candidate: TireRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireRollups(store: TireRollupStore, query: TireRollupQuery): MutableList<TireRollup> {
    return query.apply(store.list())
}
