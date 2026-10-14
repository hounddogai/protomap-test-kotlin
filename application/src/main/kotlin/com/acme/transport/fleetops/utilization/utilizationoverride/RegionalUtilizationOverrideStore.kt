package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional utilization overrides by code. */
interface RegionalUtilizationOverrideStore {
    fun find(code: String): RegionalUtilizationOverride?
    fun save(item: RegionalUtilizationOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalUtilizationOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalUtilizationOverrideStore(
    var items: MutableMap<String, RegionalUtilizationOverride>,
    var order: MutableList<String>,
) : RegionalUtilizationOverrideStore {
    override fun find(code: String): RegionalUtilizationOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalUtilizationOverride) {
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

    override fun list(): MutableList<RegionalUtilizationOverride> {
        val out = mutableListOf<RegionalUtilizationOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalUtilizationOverrideStore(): RegionalUtilizationOverrideStore {
    return MemoryRegionalUtilizationOverrideStore(
        mutableMapOf<String, RegionalUtilizationOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalUtilizationOverrideStore(
    var inner: RegionalUtilizationOverrideStore,
    var cache: TtlCache<RegionalUtilizationOverride>,
    var hits: Int,
    var misses: Int,
) : RegionalUtilizationOverrideStore {
    override fun find(code: String): RegionalUtilizationOverride? {
        val hit = (cache.get(code) as RegionalUtilizationOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalUtilizationOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalUtilizationOverride> = inner.list()
}

fun newCachedRegionalUtilizationOverrideStore(inner: RegionalUtilizationOverrideStore, ttl: Int, clock: Clock): RegionalUtilizationOverrideStore {
    return CachedRegionalUtilizationOverrideStore(
        inner = inner,
        cache = newTtlCache<RegionalUtilizationOverride>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional utilization overrides. Empty fields match all. */
data class RegionalUtilizationOverrideQuery(
    var source: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalUtilizationOverride): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalUtilizationOverride>): MutableList<RegionalUtilizationOverride> {
        val matched = filter(items) { candidate: RegionalUtilizationOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalUtilizationOverrides(store: RegionalUtilizationOverrideStore, query: RegionalUtilizationOverrideQuery): MutableList<RegionalUtilizationOverride> {
    return query.apply(store.list())
}
