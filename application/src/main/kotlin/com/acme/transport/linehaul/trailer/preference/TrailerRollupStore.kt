package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps trailer rollups by code. */
interface TrailerRollupStore {
    fun find(code: String): TrailerRollup?
    fun save(item: TrailerRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerRollupStore(
    var items: MutableMap<String, TrailerRollup>,
    var order: MutableList<String>,
) : TrailerRollupStore {
    override fun find(code: String): TrailerRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerRollup) {
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

    override fun list(): MutableList<TrailerRollup> {
        val out = mutableListOf<TrailerRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerRollupStore(): TrailerRollupStore {
    return MemoryTrailerRollupStore(mutableMapOf<String, TrailerRollup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerRollupStore(
    var inner: TrailerRollupStore,
    var cache: TtlCache<TrailerRollup>,
    var hits: Int,
    var misses: Int,
) : TrailerRollupStore {
    override fun find(code: String): TrailerRollup? {
        val hit = (cache.get(code) as TrailerRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerRollup> = inner.list()
}

fun newCachedTrailerRollupStore(inner: TrailerRollupStore, ttl: Int, clock: Clock): TrailerRollupStore {
    return CachedTrailerRollupStore(
        inner = inner,
        cache = newTtlCache<TrailerRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer rollups. Empty fields match all. */
data class TrailerRollupQuery(
    var lane: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerRollup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerRollup>): MutableList<TrailerRollup> {
        val matched = filter(items) { candidate: TrailerRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerRollups(store: TrailerRollupStore, query: TrailerRollupQuery): MutableList<TrailerRollup> {
    return query.apply(store.list())
}
