package com.acme.transport.network.zone.claim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback zone thresholds by code. */
interface FallbackZoneThresholdStore {
    fun find(code: String): FallbackZoneThreshold?
    fun save(item: FallbackZoneThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackZoneThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackZoneThresholdStore(
    var items: MutableMap<String, FallbackZoneThreshold>,
    var order: MutableList<String>,
) : FallbackZoneThresholdStore {
    override fun find(code: String): FallbackZoneThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackZoneThreshold) {
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

    override fun list(): MutableList<FallbackZoneThreshold> {
        val out = mutableListOf<FallbackZoneThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackZoneThresholdStore(): FallbackZoneThresholdStore {
    return MemoryFallbackZoneThresholdStore(
        mutableMapOf<String, FallbackZoneThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackZoneThresholdStore(
    var inner: FallbackZoneThresholdStore,
    var cache: TtlCache<FallbackZoneThreshold>,
    var hits: Int,
    var misses: Int,
) : FallbackZoneThresholdStore {
    override fun find(code: String): FallbackZoneThreshold? {
        val hit = (cache.get(code) as FallbackZoneThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackZoneThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackZoneThreshold> = inner.list()
}

fun newCachedFallbackZoneThresholdStore(inner: FallbackZoneThresholdStore, ttl: Int, clock: Clock): FallbackZoneThresholdStore {
    return CachedFallbackZoneThresholdStore(
        inner = inner,
        cache = newTtlCache<FallbackZoneThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback zone thresholds. Empty fields match all. */
data class FallbackZoneThresholdQuery(
    var unit: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackZoneThreshold): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackZoneThreshold>): MutableList<FallbackZoneThreshold> {
        val matched = filter(items) { candidate: FallbackZoneThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackZoneThresholds(store: FallbackZoneThresholdStore, query: FallbackZoneThresholdQuery): MutableList<FallbackZoneThreshold> {
    return query.apply(store.list())
}
