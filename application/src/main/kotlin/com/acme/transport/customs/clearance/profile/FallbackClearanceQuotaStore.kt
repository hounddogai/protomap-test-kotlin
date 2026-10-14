package com.acme.transport.customs.clearance.profile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback clearance quotas by code. */
interface FallbackClearanceQuotaStore {
    fun find(code: String): FallbackClearanceQuota?
    fun save(item: FallbackClearanceQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackClearanceQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackClearanceQuotaStore(
    var items: MutableMap<String, FallbackClearanceQuota>,
    var order: MutableList<String>,
) : FallbackClearanceQuotaStore {
    override fun find(code: String): FallbackClearanceQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackClearanceQuota) {
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

    override fun list(): MutableList<FallbackClearanceQuota> {
        val out = mutableListOf<FallbackClearanceQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackClearanceQuotaStore(): FallbackClearanceQuotaStore {
    return MemoryFallbackClearanceQuotaStore(
        mutableMapOf<String, FallbackClearanceQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackClearanceQuotaStore(
    var inner: FallbackClearanceQuotaStore,
    var cache: TtlCache<FallbackClearanceQuota>,
    var hits: Int,
    var misses: Int,
) : FallbackClearanceQuotaStore {
    override fun find(code: String): FallbackClearanceQuota? {
        val hit = (cache.get(code) as FallbackClearanceQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackClearanceQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackClearanceQuota> = inner.list()
}

fun newCachedFallbackClearanceQuotaStore(inner: FallbackClearanceQuotaStore, ttl: Int, clock: Clock): FallbackClearanceQuotaStore {
    return CachedFallbackClearanceQuotaStore(
        inner = inner,
        cache = newTtlCache<FallbackClearanceQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback clearance quotas. Empty fields match all. */
data class FallbackClearanceQuotaQuery(
    var label: String,
    var minCeiling: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackClearanceQuota): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.ceiling < minCeiling) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackClearanceQuota>): MutableList<FallbackClearanceQuota> {
        val matched = filter(items) { candidate: FallbackClearanceQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackClearanceQuotas(store: FallbackClearanceQuotaStore, query: FallbackClearanceQuotaQuery): MutableList<FallbackClearanceQuota> {
    return query.apply(store.list())
}
