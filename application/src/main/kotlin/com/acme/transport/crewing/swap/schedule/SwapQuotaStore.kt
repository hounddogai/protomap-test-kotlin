package com.acme.transport.crewing.swap.schedule

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps swap quotas by code. */
interface SwapQuotaStore {
    fun find(code: String): SwapQuota?
    fun save(item: SwapQuota)
    fun remove(code: String): Boolean
    fun list(): MutableList<SwapQuota>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemorySwapQuotaStore(
    var items: MutableMap<String, SwapQuota>,
    var order: MutableList<String>,
) : SwapQuotaStore {
    override fun find(code: String): SwapQuota? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: SwapQuota) {
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

    override fun list(): MutableList<SwapQuota> {
        val out = mutableListOf<SwapQuota>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemorySwapQuotaStore(): SwapQuotaStore {
    return MemorySwapQuotaStore(mutableMapOf<String, SwapQuota>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedSwapQuotaStore(
    var inner: SwapQuotaStore,
    var cache: TtlCache<SwapQuota>,
    var hits: Int,
    var misses: Int,
) : SwapQuotaStore {
    override fun find(code: String): SwapQuota? {
        val hit = (cache.get(code) as SwapQuota?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: SwapQuota) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<SwapQuota> = inner.list()
}

fun newCachedSwapQuotaStore(inner: SwapQuotaStore, ttl: Int, clock: Clock): SwapQuotaStore {
    return CachedSwapQuotaStore(
        inner = inner,
        cache = newTtlCache<SwapQuota>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects swap quotas. Empty fields match all. */
data class SwapQuotaQuery(
    var locale: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: SwapQuota): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<SwapQuota>): MutableList<SwapQuota> {
        val matched = filter(items) { candidate: SwapQuota -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findSwapQuotas(store: SwapQuotaStore, query: SwapQuotaQuery): MutableList<SwapQuota> {
    return query.apply(store.list())
}
