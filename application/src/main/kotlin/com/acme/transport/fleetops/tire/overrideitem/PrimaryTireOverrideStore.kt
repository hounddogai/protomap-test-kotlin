package com.acme.transport.fleetops.tire.overrideitem

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary tire overrides by code. */
interface PrimaryTireOverrideStore {
    fun find(code: String): PrimaryTireOverride?
    fun save(item: PrimaryTireOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryTireOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryTireOverrideStore(
    var items: MutableMap<String, PrimaryTireOverride>,
    var order: MutableList<String>,
) : PrimaryTireOverrideStore {
    override fun find(code: String): PrimaryTireOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryTireOverride) {
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

    override fun list(): MutableList<PrimaryTireOverride> {
        val out = mutableListOf<PrimaryTireOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryTireOverrideStore(): PrimaryTireOverrideStore {
    return MemoryPrimaryTireOverrideStore(mutableMapOf<String, PrimaryTireOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryTireOverrideStore(
    var inner: PrimaryTireOverrideStore,
    var cache: TtlCache<PrimaryTireOverride>,
    var hits: Int,
    var misses: Int,
) : PrimaryTireOverrideStore {
    override fun find(code: String): PrimaryTireOverride? {
        val hit = (cache.get(code) as PrimaryTireOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryTireOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryTireOverride> = inner.list()
}

fun newCachedPrimaryTireOverrideStore(inner: PrimaryTireOverrideStore, ttl: Int, clock: Clock): PrimaryTireOverrideStore {
    return CachedPrimaryTireOverrideStore(
        inner = inner,
        cache = newTtlCache<PrimaryTireOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary tire overrides. Empty fields match all. */
data class PrimaryTireOverrideQuery(
    var name: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryTireOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryTireOverride>): MutableList<PrimaryTireOverride> {
        val matched = filter(items) { candidate: PrimaryTireOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryTireOverrides(store: PrimaryTireOverrideStore, query: PrimaryTireOverrideQuery): MutableList<PrimaryTireOverride> {
    return query.apply(store.list())
}
