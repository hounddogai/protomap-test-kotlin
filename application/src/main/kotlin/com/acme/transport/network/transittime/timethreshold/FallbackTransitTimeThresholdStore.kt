package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback transit time thresholds by code. */
interface FallbackTransitTimeThresholdStore {
    fun find(code: String): FallbackTransitTimeThreshold?
    fun save(item: FallbackTransitTimeThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackTransitTimeThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackTransitTimeThresholdStore(
    var items: MutableMap<String, FallbackTransitTimeThreshold>,
    var order: MutableList<String>,
) : FallbackTransitTimeThresholdStore {
    override fun find(code: String): FallbackTransitTimeThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackTransitTimeThreshold) {
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

    override fun list(): MutableList<FallbackTransitTimeThreshold> {
        val out = mutableListOf<FallbackTransitTimeThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackTransitTimeThresholdStore(): FallbackTransitTimeThresholdStore {
    return MemoryFallbackTransitTimeThresholdStore(
        mutableMapOf<String, FallbackTransitTimeThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackTransitTimeThresholdStore(
    var inner: FallbackTransitTimeThresholdStore,
    var cache: TtlCache<FallbackTransitTimeThreshold>,
    var hits: Int,
    var misses: Int,
) : FallbackTransitTimeThresholdStore {
    override fun find(code: String): FallbackTransitTimeThreshold? {
        val hit = (cache.get(code) as FallbackTransitTimeThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackTransitTimeThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackTransitTimeThreshold> = inner.list()
}

fun newCachedFallbackTransitTimeThresholdStore(inner: FallbackTransitTimeThresholdStore, ttl: Int, clock: Clock): FallbackTransitTimeThresholdStore {
    return CachedFallbackTransitTimeThresholdStore(
        inner = inner,
        cache = newTtlCache<FallbackTransitTimeThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback transit time thresholds. Empty fields match all. */
data class FallbackTransitTimeThresholdQuery(
    var source: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackTransitTimeThreshold): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackTransitTimeThreshold>): MutableList<FallbackTransitTimeThreshold> {
        val matched = filter(items) { candidate: FallbackTransitTimeThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackTransitTimeThresholds(store: FallbackTransitTimeThresholdStore, query: FallbackTransitTimeThresholdQuery): MutableList<FallbackTransitTimeThreshold> {
    return query.apply(store.list())
}
