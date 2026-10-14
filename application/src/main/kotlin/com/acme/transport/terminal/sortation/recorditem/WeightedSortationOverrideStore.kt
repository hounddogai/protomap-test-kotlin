package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps weighted sortation overrides by code. */
interface WeightedSortationOverrideStore {
    fun find(code: String): WeightedSortationOverride?
    fun save(item: WeightedSortationOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedSortationOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedSortationOverrideStore(
    var items: MutableMap<String, WeightedSortationOverride>,
    var order: MutableList<String>,
) : WeightedSortationOverrideStore {
    override fun find(code: String): WeightedSortationOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedSortationOverride) {
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

    override fun list(): MutableList<WeightedSortationOverride> {
        val out = mutableListOf<WeightedSortationOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedSortationOverrideStore(): WeightedSortationOverrideStore {
    return MemoryWeightedSortationOverrideStore(
        mutableMapOf<String, WeightedSortationOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedSortationOverrideStore(
    var inner: WeightedSortationOverrideStore,
    var cache: TtlCache<WeightedSortationOverride>,
    var hits: Int,
    var misses: Int,
) : WeightedSortationOverrideStore {
    override fun find(code: String): WeightedSortationOverride? {
        val hit = (cache.get(code) as WeightedSortationOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedSortationOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedSortationOverride> = inner.list()
}

fun newCachedWeightedSortationOverrideStore(inner: WeightedSortationOverrideStore, ttl: Int, clock: Clock): WeightedSortationOverrideStore {
    return CachedWeightedSortationOverrideStore(
        inner = inner,
        cache = newTtlCache<WeightedSortationOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted sortation overrides. Empty fields match all. */
data class WeightedSortationOverrideQuery(
    var name: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedSortationOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<WeightedSortationOverride>): MutableList<WeightedSortationOverride> {
        val matched = filter(items) { candidate: WeightedSortationOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedSortationOverrides(store: WeightedSortationOverrideStore, query: WeightedSortationOverrideQuery): MutableList<WeightedSortationOverride> {
    return query.apply(store.list())
}
