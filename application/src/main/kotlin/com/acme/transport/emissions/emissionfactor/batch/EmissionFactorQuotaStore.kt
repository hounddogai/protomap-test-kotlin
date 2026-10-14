package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor quotas by code. */
interface EmissionFactorQuotaStore {
    fun find(code: String): EmissionFactorQuota?
    fun save(item: EmissionFactorQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorQuotaStore(
    var items: MutableMap<String, EmissionFactorQuota>,
    var order: MutableList<String>,
) : EmissionFactorQuotaStore {
    override fun find(code: String): EmissionFactorQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorQuota) {
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

    override fun list(): MutableList<EmissionFactorQuota> {
        val out = mutableListOf<EmissionFactorQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorQuotaStore(): EmissionFactorQuotaStore {
    return MemoryEmissionFactorQuotaStore(mutableMapOf<String, EmissionFactorQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorQuotaStore(
    var inner: EmissionFactorQuotaStore,
    var cache: TtlCache<EmissionFactorQuota>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorQuotaStore {
    override fun find(code: String): EmissionFactorQuota? {
        val hit = (cache.get(code) as EmissionFactorQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorQuota> = inner.list()
}

fun newCachedEmissionFactorQuotaStore(inner: EmissionFactorQuotaStore, ttl: Int, clock: Clock): EmissionFactorQuotaStore {
    return CachedEmissionFactorQuotaStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor quotas. Empty fields match all. */
data class EmissionFactorQuotaQuery(
    var lane: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorQuota): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorQuota>): MutableList<EmissionFactorQuota> {
        val matched = filter(items) { candidate: EmissionFactorQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorQuotas(store: EmissionFactorQuotaStore, query: EmissionFactorQuotaQuery): MutableList<EmissionFactorQuota> {
    return query.apply(store.list())
}
