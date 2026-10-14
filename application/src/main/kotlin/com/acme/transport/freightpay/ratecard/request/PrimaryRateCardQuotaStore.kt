package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps primary rate card quotas by code. */
interface PrimaryRateCardQuotaStore {
    fun find(code: String): PrimaryRateCardQuota?
    fun save(item: PrimaryRateCardQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryRateCardQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryRateCardQuotaStore(
    var items: MutableMap<String, PrimaryRateCardQuota>,
    var order: MutableList<String>,
) : PrimaryRateCardQuotaStore {
    override fun find(code: String): PrimaryRateCardQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryRateCardQuota) {
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

    override fun list(): MutableList<PrimaryRateCardQuota> {
        val out = mutableListOf<PrimaryRateCardQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryRateCardQuotaStore(): PrimaryRateCardQuotaStore {
    return MemoryPrimaryRateCardQuotaStore(
        mutableMapOf<String, PrimaryRateCardQuota>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryRateCardQuotaStore(
    var inner: PrimaryRateCardQuotaStore,
    var cache: TtlCache<PrimaryRateCardQuota>,
    var hits: Int,
    var misses: Int,
) : PrimaryRateCardQuotaStore {
    override fun find(code: String): PrimaryRateCardQuota? {
        val hit = (cache.get(code) as PrimaryRateCardQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryRateCardQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryRateCardQuota> = inner.list()
}

fun newCachedPrimaryRateCardQuotaStore(inner: PrimaryRateCardQuotaStore, ttl: Int, clock: Clock): PrimaryRateCardQuotaStore {
    return CachedPrimaryRateCardQuotaStore(
        inner = inner,
        cache = newTtlCache<PrimaryRateCardQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary rate card quotas. Empty fields match all. */
data class PrimaryRateCardQuotaQuery(
    var segment: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryRateCardQuota): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<PrimaryRateCardQuota>): MutableList<PrimaryRateCardQuota> {
        val matched = filter(items) { candidate: PrimaryRateCardQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryRateCardQuotas(store: PrimaryRateCardQuotaStore, query: PrimaryRateCardQuotaQuery): MutableList<PrimaryRateCardQuota> {
    return query.apply(store.list())
}
