package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps tiered backhaul rollups by code. */
interface TieredBackhaulRollupStore {
    fun find(code: String): TieredBackhaulRollup?
    fun save(item: TieredBackhaulRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredBackhaulRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredBackhaulRollupStore(
    var items: MutableMap<String, TieredBackhaulRollup>,
    var order: MutableList<String>,
) : TieredBackhaulRollupStore {
    override fun find(code: String): TieredBackhaulRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredBackhaulRollup) {
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

    override fun list(): MutableList<TieredBackhaulRollup> {
        val out = mutableListOf<TieredBackhaulRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredBackhaulRollupStore(): TieredBackhaulRollupStore {
    return MemoryTieredBackhaulRollupStore(
        mutableMapOf<String, TieredBackhaulRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredBackhaulRollupStore(
    var inner: TieredBackhaulRollupStore,
    var cache: TtlCache<TieredBackhaulRollup>,
    var hits: Int,
    var misses: Int,
) : TieredBackhaulRollupStore {
    override fun find(code: String): TieredBackhaulRollup? {
        val hit = (cache.get(code) as TieredBackhaulRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredBackhaulRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredBackhaulRollup> = inner.list()
}

fun newCachedTieredBackhaulRollupStore(inner: TieredBackhaulRollupStore, ttl: Int, clock: Clock): TieredBackhaulRollupStore {
    return CachedTieredBackhaulRollupStore(
        inner = inner,
        cache = newTtlCache<TieredBackhaulRollup>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered backhaul rollups. Empty fields match all. */
data class TieredBackhaulRollupQuery(
    var country: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredBackhaulRollup): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<TieredBackhaulRollup>): MutableList<TieredBackhaulRollup> {
        val matched = filter(items) { candidate: TieredBackhaulRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredBackhaulRollups(store: TieredBackhaulRollupStore, query: TieredBackhaulRollupQuery): MutableList<TieredBackhaulRollup> {
    return query.apply(store.list())
}
