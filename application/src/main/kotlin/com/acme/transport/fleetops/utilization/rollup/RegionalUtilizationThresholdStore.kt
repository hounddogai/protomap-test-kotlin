package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional utilization thresholds by code. */
interface RegionalUtilizationThresholdStore {
    fun find(code: String): RegionalUtilizationThreshold?
    fun save(item: RegionalUtilizationThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalUtilizationThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalUtilizationThresholdStore(
    var items: MutableMap<String, RegionalUtilizationThreshold>,
    var order: MutableList<String>,
) : RegionalUtilizationThresholdStore {
    override fun find(code: String): RegionalUtilizationThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalUtilizationThreshold) {
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

    override fun list(): MutableList<RegionalUtilizationThreshold> {
        val out = mutableListOf<RegionalUtilizationThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalUtilizationThresholdStore(): RegionalUtilizationThresholdStore {
    return MemoryRegionalUtilizationThresholdStore(
        mutableMapOf<String, RegionalUtilizationThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalUtilizationThresholdStore(
    var inner: RegionalUtilizationThresholdStore,
    var cache: TtlCache<RegionalUtilizationThreshold>,
    var hits: Int,
    var misses: Int,
) : RegionalUtilizationThresholdStore {
    override fun find(code: String): RegionalUtilizationThreshold? {
        val hit = (cache.get(code) as RegionalUtilizationThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalUtilizationThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalUtilizationThreshold> = inner.list()
}

fun newCachedRegionalUtilizationThresholdStore(inner: RegionalUtilizationThresholdStore, ttl: Int, clock: Clock): RegionalUtilizationThresholdStore {
    return CachedRegionalUtilizationThresholdStore(
        inner = inner,
        cache = newTtlCache<RegionalUtilizationThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional utilization thresholds. Empty fields match all. */
data class RegionalUtilizationThresholdQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalUtilizationThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalUtilizationThreshold>): MutableList<RegionalUtilizationThreshold> {
        val matched = filter(items) { candidate: RegionalUtilizationThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalUtilizationThresholds(store: RegionalUtilizationThresholdStore, query: RegionalUtilizationThresholdQuery): MutableList<RegionalUtilizationThreshold> {
    return query.apply(store.list())
}
