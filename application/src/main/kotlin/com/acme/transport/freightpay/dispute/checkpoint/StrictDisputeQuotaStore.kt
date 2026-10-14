package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict dispute quotas by code. */
interface StrictDisputeQuotaStore {
    fun find(code: String): StrictDisputeQuota?
    fun save(item: StrictDisputeQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDisputeQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDisputeQuotaStore(
    var items: MutableMap<String, StrictDisputeQuota>,
    var order: MutableList<String>,
) : StrictDisputeQuotaStore {
    override fun find(code: String): StrictDisputeQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDisputeQuota) {
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

    override fun list(): MutableList<StrictDisputeQuota> {
        val out = mutableListOf<StrictDisputeQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDisputeQuotaStore(): StrictDisputeQuotaStore {
    return MemoryStrictDisputeQuotaStore(mutableMapOf<String, StrictDisputeQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDisputeQuotaStore(
    var inner: StrictDisputeQuotaStore,
    var cache: TtlCache<StrictDisputeQuota>,
    var hits: Int,
    var misses: Int,
) : StrictDisputeQuotaStore {
    override fun find(code: String): StrictDisputeQuota? {
        val hit = (cache.get(code) as StrictDisputeQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDisputeQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDisputeQuota> = inner.list()
}

fun newCachedStrictDisputeQuotaStore(inner: StrictDisputeQuotaStore, ttl: Int, clock: Clock): StrictDisputeQuotaStore {
    return CachedStrictDisputeQuotaStore(
        inner = inner,
        cache = newTtlCache<StrictDisputeQuota>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict dispute quotas. Empty fields match all. */
data class StrictDisputeQuotaQuery(
    var title: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDisputeQuota): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDisputeQuota>): MutableList<StrictDisputeQuota> {
        val matched = filter(items) { candidate: StrictDisputeQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDisputeQuotas(store: StrictDisputeQuotaStore, query: StrictDisputeQuotaQuery): MutableList<StrictDisputeQuota> {
    return query.apply(store.list())
}
