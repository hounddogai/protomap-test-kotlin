package com.acme.transport.network.servicearea.milestone

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps fallback service area quotas by code. */
interface FallbackServiceAreaQuotaStore {
    fun find(code: String): FallbackServiceAreaQuota?
    fun save(item: FallbackServiceAreaQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackServiceAreaQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackServiceAreaQuotaStore(
    var items: MutableMap<String, FallbackServiceAreaQuota>,
    var order: MutableList<String>,
) : FallbackServiceAreaQuotaStore {
    override fun find(code: String): FallbackServiceAreaQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackServiceAreaQuota) {
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

    override fun list(): MutableList<FallbackServiceAreaQuota> {
        val out = mutableListOf<FallbackServiceAreaQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackServiceAreaQuotaStore(): FallbackServiceAreaQuotaStore {
    return MemoryFallbackServiceAreaQuotaStore(
        mutableMapOf<String, FallbackServiceAreaQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackServiceAreaQuotaStore(
    var inner: FallbackServiceAreaQuotaStore,
    var cache: TtlCache<FallbackServiceAreaQuota>,
    var hits: Int,
    var misses: Int,
) : FallbackServiceAreaQuotaStore {
    override fun find(code: String): FallbackServiceAreaQuota? {
        val hit = (cache.get(code) as FallbackServiceAreaQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackServiceAreaQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackServiceAreaQuota> = inner.list()
}

fun newCachedFallbackServiceAreaQuotaStore(inner: FallbackServiceAreaQuotaStore, ttl: Int, clock: Clock): FallbackServiceAreaQuotaStore {
    return CachedFallbackServiceAreaQuotaStore(
        inner = inner,
        cache = newTtlCache<FallbackServiceAreaQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback service area quotas. Empty fields match all. */
data class FallbackServiceAreaQuotaQuery(
    var unit: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackServiceAreaQuota): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
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

    fun apply(items: MutableList<FallbackServiceAreaQuota>): MutableList<FallbackServiceAreaQuota> {
        val matched = filter(items) { candidate: FallbackServiceAreaQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackServiceAreaQuotas(store: FallbackServiceAreaQuotaStore, query: FallbackServiceAreaQuotaQuery): MutableList<FallbackServiceAreaQuota> {
    return query.apply(store.list())
}
