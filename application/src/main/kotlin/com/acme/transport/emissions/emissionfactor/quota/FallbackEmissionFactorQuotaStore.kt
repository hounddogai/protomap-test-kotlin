package com.acme.transport.emissions.emissionfactor.quota

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback emission factor quotas by code. */
interface FallbackEmissionFactorQuotaStore {
    fun find(code: String): FallbackEmissionFactorQuota?
    fun save(item: FallbackEmissionFactorQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackEmissionFactorQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackEmissionFactorQuotaStore(
    var items: MutableMap<String, FallbackEmissionFactorQuota>,
    var order: MutableList<String>,
) : FallbackEmissionFactorQuotaStore {
    override fun find(code: String): FallbackEmissionFactorQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackEmissionFactorQuota) {
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

    override fun list(): MutableList<FallbackEmissionFactorQuota> {
        val out = mutableListOf<FallbackEmissionFactorQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackEmissionFactorQuotaStore(): FallbackEmissionFactorQuotaStore {
    return MemoryFallbackEmissionFactorQuotaStore(
        mutableMapOf<String, FallbackEmissionFactorQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackEmissionFactorQuotaStore(
    var inner: FallbackEmissionFactorQuotaStore,
    var cache: TtlCache<FallbackEmissionFactorQuota>,
    var hits: Int,
    var misses: Int,
) : FallbackEmissionFactorQuotaStore {
    override fun find(code: String): FallbackEmissionFactorQuota? {
        val hit = (cache.get(code) as FallbackEmissionFactorQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackEmissionFactorQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackEmissionFactorQuota> = inner.list()
}

fun newCachedFallbackEmissionFactorQuotaStore(inner: FallbackEmissionFactorQuotaStore, ttl: Int, clock: Clock): FallbackEmissionFactorQuotaStore {
    return CachedFallbackEmissionFactorQuotaStore(
        inner = inner,
        cache = newTtlCache<FallbackEmissionFactorQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback emission factor quotas. Empty fields match all. */
data class FallbackEmissionFactorQuotaQuery(
    var locale: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackEmissionFactorQuota): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackEmissionFactorQuota>): MutableList<FallbackEmissionFactorQuota> {
        val matched = filter(items) { candidate: FallbackEmissionFactorQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackEmissionFactorQuotas(store: FallbackEmissionFactorQuotaStore, query: FallbackEmissionFactorQuotaQuery): MutableList<FallbackEmissionFactorQuota> {
    return query.apply(store.list())
}
