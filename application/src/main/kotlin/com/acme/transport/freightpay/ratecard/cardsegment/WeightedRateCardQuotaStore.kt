package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted rate card quotas by code. */
interface WeightedRateCardQuotaStore {
    fun find(code: String): WeightedRateCardQuota?
    fun save(item: WeightedRateCardQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRateCardQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRateCardQuotaStore(
    var items: MutableMap<String, WeightedRateCardQuota>,
    var order: MutableList<String>,
) : WeightedRateCardQuotaStore {
    override fun find(code: String): WeightedRateCardQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRateCardQuota) {
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

    override fun list(): MutableList<WeightedRateCardQuota> {
        val out = mutableListOf<WeightedRateCardQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRateCardQuotaStore(): WeightedRateCardQuotaStore {
    return MemoryWeightedRateCardQuotaStore(
        mutableMapOf<String, WeightedRateCardQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRateCardQuotaStore(
    var inner: WeightedRateCardQuotaStore,
    var cache: TtlCache<WeightedRateCardQuota>,
    var hits: Int,
    var misses: Int,
) : WeightedRateCardQuotaStore {
    override fun find(code: String): WeightedRateCardQuota? {
        val hit = (cache.get(code) as WeightedRateCardQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRateCardQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRateCardQuota> = inner.list()
}

fun newCachedWeightedRateCardQuotaStore(inner: WeightedRateCardQuotaStore, ttl: Int, clock: Clock): WeightedRateCardQuotaStore {
    return CachedWeightedRateCardQuotaStore(
        inner = inner,
        cache = newTtlCache<WeightedRateCardQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted rate card quotas. Empty fields match all. */
data class WeightedRateCardQuotaQuery(
    var source: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRateCardQuota): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedRateCardQuota>): MutableList<WeightedRateCardQuota> {
        val matched = filter(items) { candidate: WeightedRateCardQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRateCardQuotas(store: WeightedRateCardQuotaStore, query: WeightedRateCardQuotaQuery): MutableList<WeightedRateCardQuota> {
    return query.apply(store.list())
}
