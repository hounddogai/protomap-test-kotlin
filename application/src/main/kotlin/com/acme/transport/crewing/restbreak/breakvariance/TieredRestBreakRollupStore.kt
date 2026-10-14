package com.acme.transport.crewing.restbreak.breakvariance

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered rest break rollups by code. */
interface TieredRestBreakRollupStore {
    fun find(code: String): TieredRestBreakRollup?
    fun save(item: TieredRestBreakRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredRestBreakRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredRestBreakRollupStore(
    var items: MutableMap<String, TieredRestBreakRollup>,
    var order: MutableList<String>,
) : TieredRestBreakRollupStore {
    override fun find(code: String): TieredRestBreakRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredRestBreakRollup) {
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

    override fun list(): MutableList<TieredRestBreakRollup> {
        val out = mutableListOf<TieredRestBreakRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredRestBreakRollupStore(): TieredRestBreakRollupStore {
    return MemoryTieredRestBreakRollupStore(
        mutableMapOf<String, TieredRestBreakRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredRestBreakRollupStore(
    var inner: TieredRestBreakRollupStore,
    var cache: TtlCache<TieredRestBreakRollup>,
    var hits: Int,
    var misses: Int,
) : TieredRestBreakRollupStore {
    override fun find(code: String): TieredRestBreakRollup? {
        val hit = (cache.get(code) as TieredRestBreakRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredRestBreakRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredRestBreakRollup> = inner.list()
}

fun newCachedTieredRestBreakRollupStore(inner: TieredRestBreakRollupStore, ttl: Int, clock: Clock): TieredRestBreakRollupStore {
    return CachedTieredRestBreakRollupStore(
        inner = inner,
        cache = newTtlCache<TieredRestBreakRollup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered rest break rollups. Empty fields match all. */
data class TieredRestBreakRollupQuery(
    var country: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredRestBreakRollup): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredRestBreakRollup>): MutableList<TieredRestBreakRollup> {
        val matched = filter(items) { candidate: TieredRestBreakRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredRestBreakRollups(store: TieredRestBreakRollupStore, query: TieredRestBreakRollupQuery): MutableList<TieredRestBreakRollup> {
    return query.apply(store.list())
}
