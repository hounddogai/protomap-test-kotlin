package com.acme.transport.terminal.traileryard.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard rollups by code. */
interface TrailerYardRollupStore {
    fun find(code: String): TrailerYardRollup?
    fun save(item: TrailerYardRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardRollupStore(
    var items: MutableMap<String, TrailerYardRollup>,
    var order: MutableList<String>,
) : TrailerYardRollupStore {
    override fun find(code: String): TrailerYardRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardRollup) {
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

    override fun list(): MutableList<TrailerYardRollup> {
        val out = mutableListOf<TrailerYardRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardRollupStore(): TrailerYardRollupStore {
    return MemoryTrailerYardRollupStore(mutableMapOf<String, TrailerYardRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardRollupStore(
    var inner: TrailerYardRollupStore,
    var cache: TtlCache<TrailerYardRollup>,
    var hits: Int,
    var misses: Int,
) : TrailerYardRollupStore {
    override fun find(code: String): TrailerYardRollup? {
        val hit = (cache.get(code) as TrailerYardRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardRollup> = inner.list()
}

fun newCachedTrailerYardRollupStore(inner: TrailerYardRollupStore, ttl: Int, clock: Clock): TrailerYardRollupStore {
    return CachedTrailerYardRollupStore(
        inner = inner,
        cache = newTtlCache<TrailerYardRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard rollups. Empty fields match all. */
data class TrailerYardRollupQuery(
    var segment: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardRollup): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardRollup>): MutableList<TrailerYardRollup> {
        val matched = filter(items) { candidate: TrailerYardRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardRollups(store: TrailerYardRollupStore, query: TrailerYardRollupQuery): MutableList<TrailerYardRollup> {
    return query.apply(store.list())
}
