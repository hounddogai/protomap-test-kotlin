package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered fuel burn rollups by code. */
interface TieredFuelBurnRollupStore {
    fun find(code: String): TieredFuelBurnRollup?
    fun save(item: TieredFuelBurnRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredFuelBurnRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredFuelBurnRollupStore(
    var items: MutableMap<String, TieredFuelBurnRollup>,
    var order: MutableList<String>,
) : TieredFuelBurnRollupStore {
    override fun find(code: String): TieredFuelBurnRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredFuelBurnRollup) {
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

    override fun list(): MutableList<TieredFuelBurnRollup> {
        val out = mutableListOf<TieredFuelBurnRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredFuelBurnRollupStore(): TieredFuelBurnRollupStore {
    return MemoryTieredFuelBurnRollupStore(
        mutableMapOf<String, TieredFuelBurnRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredFuelBurnRollupStore(
    var inner: TieredFuelBurnRollupStore,
    var cache: TtlCache<TieredFuelBurnRollup>,
    var hits: Int,
    var misses: Int,
) : TieredFuelBurnRollupStore {
    override fun find(code: String): TieredFuelBurnRollup? {
        val hit = (cache.get(code) as TieredFuelBurnRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredFuelBurnRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredFuelBurnRollup> = inner.list()
}

fun newCachedTieredFuelBurnRollupStore(inner: TieredFuelBurnRollupStore, ttl: Int, clock: Clock): TieredFuelBurnRollupStore {
    return CachedTieredFuelBurnRollupStore(
        inner = inner,
        cache = newTtlCache<TieredFuelBurnRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered fuel burn rollups. Empty fields match all. */
data class TieredFuelBurnRollupQuery(
    var lane: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredFuelBurnRollup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<TieredFuelBurnRollup>): MutableList<TieredFuelBurnRollup> {
        val matched = filter(items) { candidate: TieredFuelBurnRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredFuelBurnRollups(store: TieredFuelBurnRollupStore, query: TieredFuelBurnRollupQuery): MutableList<TieredFuelBurnRollup> {
    return query.apply(store.list())
}
