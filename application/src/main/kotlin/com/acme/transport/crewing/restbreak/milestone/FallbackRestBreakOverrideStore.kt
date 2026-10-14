package com.acme.transport.crewing.restbreak.milestone

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback rest break overrides by code. */
interface FallbackRestBreakOverrideStore {
    fun find(code: String): FallbackRestBreakOverride?
    fun save(item: FallbackRestBreakOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRestBreakOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRestBreakOverrideStore(
    var items: MutableMap<String, FallbackRestBreakOverride>,
    var order: MutableList<String>,
) : FallbackRestBreakOverrideStore {
    override fun find(code: String): FallbackRestBreakOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRestBreakOverride) {
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

    override fun list(): MutableList<FallbackRestBreakOverride> {
        val out = mutableListOf<FallbackRestBreakOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRestBreakOverrideStore(): FallbackRestBreakOverrideStore {
    return MemoryFallbackRestBreakOverrideStore(
        mutableMapOf<String, FallbackRestBreakOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRestBreakOverrideStore(
    var inner: FallbackRestBreakOverrideStore,
    var cache: TtlCache<FallbackRestBreakOverride>,
    var hits: Int,
    var misses: Int,
) : FallbackRestBreakOverrideStore {
    override fun find(code: String): FallbackRestBreakOverride? {
        val hit = (cache.get(code) as FallbackRestBreakOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRestBreakOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRestBreakOverride> = inner.list()
}

fun newCachedFallbackRestBreakOverrideStore(inner: FallbackRestBreakOverrideStore, ttl: Int, clock: Clock): FallbackRestBreakOverrideStore {
    return CachedFallbackRestBreakOverrideStore(
        inner = inner,
        cache = newTtlCache<FallbackRestBreakOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback rest break overrides. Empty fields match all. */
data class FallbackRestBreakOverrideQuery(
    var depot: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRestBreakOverride): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRestBreakOverride>): MutableList<FallbackRestBreakOverride> {
        val matched = filter(items) { candidate: FallbackRestBreakOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRestBreakOverrides(store: FallbackRestBreakOverrideStore, query: FallbackRestBreakOverrideQuery): MutableList<FallbackRestBreakOverride> {
    return query.apply(store.list())
}
