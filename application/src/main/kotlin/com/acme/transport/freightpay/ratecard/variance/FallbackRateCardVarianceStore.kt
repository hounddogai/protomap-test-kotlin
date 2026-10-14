package com.acme.transport.freightpay.ratecard.variance

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fallback rate card variances by code. */
interface FallbackRateCardVarianceStore {
    fun find(code: String): FallbackRateCardVariance?
    fun save(item: FallbackRateCardVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<FallbackRateCardVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFallbackRateCardVarianceStore(
    var items: MutableMap<String, FallbackRateCardVariance>,
    var order: MutableList<String>,
) : FallbackRateCardVarianceStore {
    override fun find(code: String): FallbackRateCardVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FallbackRateCardVariance) {
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

    override fun list(): MutableList<FallbackRateCardVariance> {
        val out = mutableListOf<FallbackRateCardVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFallbackRateCardVarianceStore(): FallbackRateCardVarianceStore {
    return MemoryFallbackRateCardVarianceStore(
        mutableMapOf<String, FallbackRateCardVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFallbackRateCardVarianceStore(
    var inner: FallbackRateCardVarianceStore,
    var cache: TtlCache<FallbackRateCardVariance>,
    var hits: Int,
    var misses: Int,
) : FallbackRateCardVarianceStore {
    override fun find(code: String): FallbackRateCardVariance? {
        val hit = (cache.get(code) as FallbackRateCardVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FallbackRateCardVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FallbackRateCardVariance> = inner.list()
}

fun newCachedFallbackRateCardVarianceStore(inner: FallbackRateCardVarianceStore, ttl: Int, clock: Clock): FallbackRateCardVarianceStore {
    return CachedFallbackRateCardVarianceStore(
        inner = inner,
        cache = newTtlCache<FallbackRateCardVariance>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fallback rate card variances. Empty fields match all. */
data class FallbackRateCardVarianceQuery(
    var title: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FallbackRateCardVariance): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FallbackRateCardVariance>): MutableList<FallbackRateCardVariance> {
        val matched = filter(items) { candidate: FallbackRateCardVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFallbackRateCardVariances(store: FallbackRateCardVarianceStore, query: FallbackRateCardVarianceQuery): MutableList<FallbackRateCardVariance> {
    return query.apply(store.list())
}
