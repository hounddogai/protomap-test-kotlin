package com.acme.transport.terminal.crossdock.proposal

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps cross dock rollups by code. */
interface CrossDockRollupStore {
    fun find(code: String): CrossDockRollup?
    fun save(item: CrossDockRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CrossDockRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCrossDockRollupStore(
    var items: MutableMap<String, CrossDockRollup>,
    var order: MutableList<String>,
) : CrossDockRollupStore {
    override fun find(code: String): CrossDockRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CrossDockRollup) {
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

    override fun list(): MutableList<CrossDockRollup> {
        val out = mutableListOf<CrossDockRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCrossDockRollupStore(): CrossDockRollupStore {
    return MemoryCrossDockRollupStore(mutableMapOf<String, CrossDockRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCrossDockRollupStore(
    var inner: CrossDockRollupStore,
    var cache: TtlCache<CrossDockRollup>,
    var hits: Int,
    var misses: Int,
) : CrossDockRollupStore {
    override fun find(code: String): CrossDockRollup? {
        val hit = (cache.get(code) as CrossDockRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CrossDockRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CrossDockRollup> = inner.list()
}

fun newCachedCrossDockRollupStore(inner: CrossDockRollupStore, ttl: Int, clock: Clock): CrossDockRollupStore {
    return CachedCrossDockRollupStore(
        inner = inner,
        cache = newTtlCache<CrossDockRollup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cross dock rollups. Empty fields match all. */
data class CrossDockRollupQuery(
    var segment: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CrossDockRollup): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<CrossDockRollup>): MutableList<CrossDockRollup> {
        val matched = filter(items) { candidate: CrossDockRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCrossDockRollups(store: CrossDockRollupStore, query: CrossDockRollupQuery): MutableList<CrossDockRollup> {
    return query.apply(store.list())
}
