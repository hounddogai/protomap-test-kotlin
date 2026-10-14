package com.acme.transport.terminal.crossdock.dockslot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps capped cross dock rollups by code. */
interface CappedCrossDockRollupStore {
    fun find(code: String): CappedCrossDockRollup?
    fun save(item: CappedCrossDockRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedCrossDockRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedCrossDockRollupStore(
    var items: MutableMap<String, CappedCrossDockRollup>,
    var order: MutableList<String>,
) : CappedCrossDockRollupStore {
    override fun find(code: String): CappedCrossDockRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedCrossDockRollup) {
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

    override fun list(): MutableList<CappedCrossDockRollup> {
        val out = mutableListOf<CappedCrossDockRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedCrossDockRollupStore(): CappedCrossDockRollupStore {
    return MemoryCappedCrossDockRollupStore(
        mutableMapOf<String, CappedCrossDockRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedCrossDockRollupStore(
    var inner: CappedCrossDockRollupStore,
    var cache: TtlCache<CappedCrossDockRollup>,
    var hits: Int,
    var misses: Int,
) : CappedCrossDockRollupStore {
    override fun find(code: String): CappedCrossDockRollup? {
        val hit = (cache.get(code) as CappedCrossDockRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedCrossDockRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedCrossDockRollup> = inner.list()
}

fun newCachedCappedCrossDockRollupStore(inner: CappedCrossDockRollupStore, ttl: Int, clock: Clock): CappedCrossDockRollupStore {
    return CachedCappedCrossDockRollupStore(
        inner = inner,
        cache = newTtlCache<CappedCrossDockRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped cross dock rollups. Empty fields match all. */
data class CappedCrossDockRollupQuery(
    var name: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedCrossDockRollup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<CappedCrossDockRollup>): MutableList<CappedCrossDockRollup> {
        val matched = filter(items) { candidate: CappedCrossDockRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedCrossDockRollups(store: CappedCrossDockRollupStore, query: CappedCrossDockRollupQuery): MutableList<CappedCrossDockRollup> {
    return query.apply(store.list())
}
