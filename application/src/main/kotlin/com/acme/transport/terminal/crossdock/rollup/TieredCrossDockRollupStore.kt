package com.acme.transport.terminal.crossdock.rollup

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps tiered cross dock rollups by code. */
interface TieredCrossDockRollupStore {
    fun find(code: String): TieredCrossDockRollup?
    fun save(item: TieredCrossDockRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredCrossDockRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredCrossDockRollupStore(
    var items: MutableMap<String, TieredCrossDockRollup>,
    var order: MutableList<String>,
) : TieredCrossDockRollupStore {
    override fun find(code: String): TieredCrossDockRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredCrossDockRollup) {
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

    override fun list(): MutableList<TieredCrossDockRollup> {
        val out = mutableListOf<TieredCrossDockRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredCrossDockRollupStore(): TieredCrossDockRollupStore {
    return MemoryTieredCrossDockRollupStore(
        mutableMapOf<String, TieredCrossDockRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredCrossDockRollupStore(
    var inner: TieredCrossDockRollupStore,
    var cache: TtlCache<TieredCrossDockRollup>,
    var hits: Int,
    var misses: Int,
) : TieredCrossDockRollupStore {
    override fun find(code: String): TieredCrossDockRollup? {
        val hit = (cache.get(code) as TieredCrossDockRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredCrossDockRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredCrossDockRollup> = inner.list()
}

fun newCachedTieredCrossDockRollupStore(inner: TieredCrossDockRollupStore, ttl: Int, clock: Clock): TieredCrossDockRollupStore {
    return CachedTieredCrossDockRollupStore(
        inner = inner,
        cache = newTtlCache<TieredCrossDockRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered cross dock rollups. Empty fields match all. */
data class TieredCrossDockRollupQuery(
    var locale: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredCrossDockRollup): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredCrossDockRollup>): MutableList<TieredCrossDockRollup> {
        val matched = filter(items) { candidate: TieredCrossDockRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredCrossDockRollups(store: TieredCrossDockRollupStore, query: TieredCrossDockRollupQuery): MutableList<TieredCrossDockRollup> {
    return query.apply(store.list())
}
