package com.acme.transport.linehaul.capacity.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps lenient capacity rollups by code. */
interface LenientCapacityRollupStore {
    fun find(code: String): LenientCapacityRollup?
    fun save(item: LenientCapacityRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientCapacityRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientCapacityRollupStore(
    var items: MutableMap<String, LenientCapacityRollup>,
    var order: MutableList<String>,
) : LenientCapacityRollupStore {
    override fun find(code: String): LenientCapacityRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientCapacityRollup) {
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

    override fun list(): MutableList<LenientCapacityRollup> {
        val out = mutableListOf<LenientCapacityRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientCapacityRollupStore(): LenientCapacityRollupStore {
    return MemoryLenientCapacityRollupStore(
        mutableMapOf<String, LenientCapacityRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientCapacityRollupStore(
    var inner: LenientCapacityRollupStore,
    var cache: TtlCache<LenientCapacityRollup>,
    var hits: Int,
    var misses: Int,
) : LenientCapacityRollupStore {
    override fun find(code: String): LenientCapacityRollup? {
        val hit = (cache.get(code) as LenientCapacityRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientCapacityRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientCapacityRollup> = inner.list()
}

fun newCachedLenientCapacityRollupStore(inner: LenientCapacityRollupStore, ttl: Int, clock: Clock): LenientCapacityRollupStore {
    return CachedLenientCapacityRollupStore(
        inner = inner,
        cache = newTtlCache<LenientCapacityRollup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient capacity rollups. Empty fields match all. */
data class LenientCapacityRollupQuery(
    var channel: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientCapacityRollup): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientCapacityRollup>): MutableList<LenientCapacityRollup> {
        val matched = filter(items) { candidate: LenientCapacityRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientCapacityRollups(store: LenientCapacityRollupStore, query: LenientCapacityRollupQuery): MutableList<LenientCapacityRollup> {
    return query.apply(store.list())
}
