package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted rest break overrides by code. */
interface WeightedRestBreakOverrideStore {
    fun find(code: String): WeightedRestBreakOverride?
    fun save(item: WeightedRestBreakOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRestBreakOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRestBreakOverrideStore(
    var items: MutableMap<String, WeightedRestBreakOverride>,
    var order: MutableList<String>,
) : WeightedRestBreakOverrideStore {
    override fun find(code: String): WeightedRestBreakOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRestBreakOverride) {
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

    override fun list(): MutableList<WeightedRestBreakOverride> {
        val out = mutableListOf<WeightedRestBreakOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRestBreakOverrideStore(): WeightedRestBreakOverrideStore {
    return MemoryWeightedRestBreakOverrideStore(
        mutableMapOf<String, WeightedRestBreakOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRestBreakOverrideStore(
    var inner: WeightedRestBreakOverrideStore,
    var cache: TtlCache<WeightedRestBreakOverride>,
    var hits: Int,
    var misses: Int,
) : WeightedRestBreakOverrideStore {
    override fun find(code: String): WeightedRestBreakOverride? {
        val hit = (cache.get(code) as WeightedRestBreakOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRestBreakOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRestBreakOverride> = inner.list()
}

fun newCachedWeightedRestBreakOverrideStore(inner: WeightedRestBreakOverrideStore, ttl: Int, clock: Clock): WeightedRestBreakOverrideStore {
    return CachedWeightedRestBreakOverrideStore(
        inner = inner,
        cache = newTtlCache<WeightedRestBreakOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted rest break overrides. Empty fields match all. */
data class WeightedRestBreakOverrideQuery(
    var channel: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRestBreakOverride): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedRestBreakOverride>): MutableList<WeightedRestBreakOverride> {
        val matched = filter(items) { candidate: WeightedRestBreakOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRestBreakOverrides(store: WeightedRestBreakOverrideStore, query: WeightedRestBreakOverrideQuery): MutableList<WeightedRestBreakOverride> {
    return query.apply(store.list())
}
