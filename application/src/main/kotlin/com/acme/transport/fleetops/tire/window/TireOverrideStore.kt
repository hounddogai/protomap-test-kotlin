package com.acme.transport.fleetops.tire.window

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tire overrides by code. */
interface TireOverrideStore {
    fun find(code: String): TireOverride?
    fun save(item: TireOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<TireOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTireOverrideStore(
    var items: MutableMap<String, TireOverride>,
    var order: MutableList<String>,
) : TireOverrideStore {
    override fun find(code: String): TireOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TireOverride) {
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

    override fun list(): MutableList<TireOverride> {
        val out = mutableListOf<TireOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTireOverrideStore(): TireOverrideStore {
    return MemoryTireOverrideStore(mutableMapOf<String, TireOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTireOverrideStore(
    var inner: TireOverrideStore,
    var cache: TtlCache<TireOverride>,
    var hits: Int,
    var misses: Int,
) : TireOverrideStore {
    override fun find(code: String): TireOverride? {
        val hit = (cache.get(code) as TireOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TireOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TireOverride> = inner.list()
}

fun newCachedTireOverrideStore(inner: TireOverrideStore, ttl: Int, clock: Clock): TireOverrideStore {
    return CachedTireOverrideStore(
        inner = inner,
        cache = newTtlCache<TireOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tire overrides. Empty fields match all. */
data class TireOverrideQuery(
    var region: String,
    var minRevision: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TireOverride): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.revision < minRevision) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TireOverride>): MutableList<TireOverride> {
        val matched = filter(items) { candidate: TireOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTireOverrides(store: TireOverrideStore, query: TireOverrideQuery): MutableList<TireOverride> {
    return query.apply(store.list())
}
