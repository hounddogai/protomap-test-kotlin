package com.acme.transport.lastmile.drop.dropdraft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps tiered drop rollups by code. */
interface TieredDropRollupStore {
    fun find(code: String): TieredDropRollup?
    fun save(item: TieredDropRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredDropRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredDropRollupStore(
    var items: MutableMap<String, TieredDropRollup>,
    var order: MutableList<String>,
) : TieredDropRollupStore {
    override fun find(code: String): TieredDropRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredDropRollup) {
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

    override fun list(): MutableList<TieredDropRollup> {
        val out = mutableListOf<TieredDropRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredDropRollupStore(): TieredDropRollupStore {
    return MemoryTieredDropRollupStore(mutableMapOf<String, TieredDropRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredDropRollupStore(
    var inner: TieredDropRollupStore,
    var cache: TtlCache<TieredDropRollup>,
    var hits: Int,
    var misses: Int,
) : TieredDropRollupStore {
    override fun find(code: String): TieredDropRollup? {
        val hit = (cache.get(code) as TieredDropRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredDropRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredDropRollup> = inner.list()
}

fun newCachedTieredDropRollupStore(inner: TieredDropRollupStore, ttl: Int, clock: Clock): TieredDropRollupStore {
    return CachedTieredDropRollupStore(
        inner = inner,
        cache = newTtlCache<TieredDropRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered drop rollups. Empty fields match all. */
data class TieredDropRollupQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredDropRollup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<TieredDropRollup>): MutableList<TieredDropRollup> {
        val matched = filter(items) { candidate: TieredDropRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredDropRollups(store: TieredDropRollupStore, query: TieredDropRollupQuery): MutableList<TieredDropRollup> {
    return query.apply(store.list())
}
