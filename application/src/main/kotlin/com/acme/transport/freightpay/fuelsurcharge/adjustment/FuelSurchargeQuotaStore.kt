package com.acme.transport.freightpay.fuelsurcharge.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge quotas by code. */
interface FuelSurchargeQuotaStore {
    fun find(code: String): FuelSurchargeQuota?
    fun save(item: FuelSurchargeQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeQuotaStore(
    var items: MutableMap<String, FuelSurchargeQuota>,
    var order: MutableList<String>,
) : FuelSurchargeQuotaStore {
    override fun find(code: String): FuelSurchargeQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeQuota) {
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

    override fun list(): MutableList<FuelSurchargeQuota> {
        val out = mutableListOf<FuelSurchargeQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeQuotaStore(): FuelSurchargeQuotaStore {
    return MemoryFuelSurchargeQuotaStore(mutableMapOf<String, FuelSurchargeQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeQuotaStore(
    var inner: FuelSurchargeQuotaStore,
    var cache: TtlCache<FuelSurchargeQuota>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeQuotaStore {
    override fun find(code: String): FuelSurchargeQuota? {
        val hit = (cache.get(code) as FuelSurchargeQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeQuota> = inner.list()
}

fun newCachedFuelSurchargeQuotaStore(inner: FuelSurchargeQuotaStore, ttl: Int, clock: Clock): FuelSurchargeQuotaStore {
    return CachedFuelSurchargeQuotaStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeQuota>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge quotas. Empty fields match all. */
data class FuelSurchargeQuotaQuery(
    var channel: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeQuota): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<FuelSurchargeQuota>): MutableList<FuelSurchargeQuota> {
        val matched = filter(items) { candidate: FuelSurchargeQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeQuotas(store: FuelSurchargeQuotaStore, query: FuelSurchargeQuotaQuery): MutableList<FuelSurchargeQuota> {
    return query.apply(store.list())
}
