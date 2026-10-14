package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback ev charge thresholds by code. */
interface FallbackEvChargeThresholdStore {
    fun find(code: String): FallbackEvChargeThreshold?
    fun save(item: FallbackEvChargeThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEvChargeThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEvChargeThresholdStore(
    var items: MutableMap<String, FallbackEvChargeThreshold>,
    var order: MutableList<String>,
) : FallbackEvChargeThresholdStore {
    override fun find(code: String): FallbackEvChargeThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEvChargeThreshold) {
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

    override fun list(): MutableList<FallbackEvChargeThreshold> {
        val out = mutableListOf<FallbackEvChargeThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEvChargeThresholdStore(): FallbackEvChargeThresholdStore {
    return MemoryFallbackEvChargeThresholdStore(
        mutableMapOf<String, FallbackEvChargeThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEvChargeThresholdStore(
    var inner: FallbackEvChargeThresholdStore,
    var cache: TtlCache<FallbackEvChargeThreshold>,
    var hits: Int,
    var misses: Int,
) : FallbackEvChargeThresholdStore {
    override fun find(code: String): FallbackEvChargeThreshold? {
        val hit = (cache.get(code) as FallbackEvChargeThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEvChargeThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEvChargeThreshold> = inner.list()
}

fun newCachedFallbackEvChargeThresholdStore(inner: FallbackEvChargeThresholdStore, ttl: Int, clock: Clock): FallbackEvChargeThresholdStore {
    return CachedFallbackEvChargeThresholdStore(
        inner = inner,
        cache = newTtlCache<FallbackEvChargeThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback ev charge thresholds. Empty fields match all. */
data class FallbackEvChargeThresholdQuery(
    var category: String,
    var minCreatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEvChargeThreshold): Boolean {
        if (category != "" && normalize(item.category) != normalize(category)) {
            return false
        }
        if (item.createdAt < minCreatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackEvChargeThreshold>): MutableList<FallbackEvChargeThreshold> {
        val matched = filter(items) { candidate: FallbackEvChargeThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEvChargeThresholds(store: FallbackEvChargeThresholdStore, query: FallbackEvChargeThresholdQuery): MutableList<FallbackEvChargeThreshold> {
    return query.apply(store.list())
}
