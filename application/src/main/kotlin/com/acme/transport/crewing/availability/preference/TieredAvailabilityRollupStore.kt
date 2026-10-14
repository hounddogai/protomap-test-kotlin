package com.acme.transport.crewing.availability.preference

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered availability rollups by code. */
interface TieredAvailabilityRollupStore {
    fun find(code: String): TieredAvailabilityRollup?
    fun save(item: TieredAvailabilityRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredAvailabilityRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredAvailabilityRollupStore(
    var items: MutableMap<String, TieredAvailabilityRollup>,
    var order: MutableList<String>,
) : TieredAvailabilityRollupStore {
    override fun find(code: String): TieredAvailabilityRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredAvailabilityRollup) {
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

    override fun list(): MutableList<TieredAvailabilityRollup> {
        val out = mutableListOf<TieredAvailabilityRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredAvailabilityRollupStore(): TieredAvailabilityRollupStore {
    return MemoryTieredAvailabilityRollupStore(
        mutableMapOf<String, TieredAvailabilityRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredAvailabilityRollupStore(
    var inner: TieredAvailabilityRollupStore,
    var cache: TtlCache<TieredAvailabilityRollup>,
    var hits: Int,
    var misses: Int,
) : TieredAvailabilityRollupStore {
    override fun find(code: String): TieredAvailabilityRollup? {
        val hit = (cache.get(code) as TieredAvailabilityRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredAvailabilityRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredAvailabilityRollup> = inner.list()
}

fun newCachedTieredAvailabilityRollupStore(inner: TieredAvailabilityRollupStore, ttl: Int, clock: Clock): TieredAvailabilityRollupStore {
    return CachedTieredAvailabilityRollupStore(
        inner = inner,
        cache = newTtlCache<TieredAvailabilityRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered availability rollups. Empty fields match all. */
data class TieredAvailabilityRollupQuery(
    var segment: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredAvailabilityRollup): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredAvailabilityRollup>): MutableList<TieredAvailabilityRollup> {
        val matched = filter(items) { candidate: TieredAvailabilityRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredAvailabilityRollups(store: TieredAvailabilityRollupStore, query: TieredAvailabilityRollupQuery): MutableList<TieredAvailabilityRollup> {
    return query.apply(store.list())
}
