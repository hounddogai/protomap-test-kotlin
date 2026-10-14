package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card quotas by code. */
interface RateCardQuotaStore {
    fun find(code: String): RateCardQuota?
    fun save(item: RateCardQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardQuotaStore(
    var items: MutableMap<String, RateCardQuota>,
    var order: MutableList<String>,
) : RateCardQuotaStore {
    override fun find(code: String): RateCardQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardQuota) {
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

    override fun list(): MutableList<RateCardQuota> {
        val out = mutableListOf<RateCardQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardQuotaStore(): RateCardQuotaStore {
    return MemoryRateCardQuotaStore(mutableMapOf<String, RateCardQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardQuotaStore(
    var inner: RateCardQuotaStore,
    var cache: TtlCache<RateCardQuota>,
    var hits: Int,
    var misses: Int,
) : RateCardQuotaStore {
    override fun find(code: String): RateCardQuota? {
        val hit = (cache.get(code) as RateCardQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardQuota> = inner.list()
}

fun newCachedRateCardQuotaStore(inner: RateCardQuotaStore, ttl: Int, clock: Clock): RateCardQuotaStore {
    return CachedRateCardQuotaStore(
        inner = inner,
        cache = newTtlCache<RateCardQuota>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card quotas. Empty fields match all. */
data class RateCardQuotaQuery(
    var lane: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardQuota): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardQuota>): MutableList<RateCardQuota> {
        val matched = filter(items) { candidate: RateCardQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardQuotas(store: RateCardQuotaStore, query: RateCardQuotaQuery): MutableList<RateCardQuota> {
    return query.apply(store.list())
}
