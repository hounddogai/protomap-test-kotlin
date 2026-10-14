package com.acme.transport.freightpay.dispute.group

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute quotas by code. */
interface DisputeQuotaStore {
    fun find(code: String): DisputeQuota?
    fun save(item: DisputeQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeQuotaStore(
    var items: MutableMap<String, DisputeQuota>,
    var order: MutableList<String>,
) : DisputeQuotaStore {
    override fun find(code: String): DisputeQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeQuota) {
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

    override fun list(): MutableList<DisputeQuota> {
        val out = mutableListOf<DisputeQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeQuotaStore(): DisputeQuotaStore {
    return MemoryDisputeQuotaStore(mutableMapOf<String, DisputeQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeQuotaStore(
    var inner: DisputeQuotaStore,
    var cache: TtlCache<DisputeQuota>,
    var hits: Int,
    var misses: Int,
) : DisputeQuotaStore {
    override fun find(code: String): DisputeQuota? {
        val hit = (cache.get(code) as DisputeQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeQuota> = inner.list()
}

fun newCachedDisputeQuotaStore(inner: DisputeQuotaStore, ttl: Int, clock: Clock): DisputeQuotaStore {
    return CachedDisputeQuotaStore(
        inner = inner,
        cache = newTtlCache<DisputeQuota>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute quotas. Empty fields match all. */
data class DisputeQuotaQuery(
    var depot: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeQuota): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DisputeQuota>): MutableList<DisputeQuota> {
        val matched = filter(items) { candidate: DisputeQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeQuotas(store: DisputeQuotaStore, query: DisputeQuotaQuery): MutableList<DisputeQuota> {
    return query.apply(store.list())
}
