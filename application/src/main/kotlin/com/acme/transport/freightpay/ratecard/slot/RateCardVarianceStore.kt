package com.acme.transport.freightpay.ratecard.slot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card variances by code. */
interface RateCardVarianceStore {
    fun find(code: String): RateCardVariance?
    fun save(item: RateCardVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardVarianceStore(
    var items: MutableMap<String, RateCardVariance>,
    var order: MutableList<String>,
) : RateCardVarianceStore {
    override fun find(code: String): RateCardVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardVariance) {
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

    override fun list(): MutableList<RateCardVariance> {
        val out = mutableListOf<RateCardVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardVarianceStore(): RateCardVarianceStore {
    return MemoryRateCardVarianceStore(mutableMapOf<String, RateCardVariance>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardVarianceStore(
    var inner: RateCardVarianceStore,
    var cache: TtlCache<RateCardVariance>,
    var hits: Int,
    var misses: Int,
) : RateCardVarianceStore {
    override fun find(code: String): RateCardVariance? {
        val hit = (cache.get(code) as RateCardVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardVariance> = inner.list()
}

fun newCachedRateCardVarianceStore(inner: RateCardVarianceStore, ttl: Int, clock: Clock): RateCardVarianceStore {
    return CachedRateCardVarianceStore(
        inner = inner,
        cache = newTtlCache<RateCardVariance>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card variances. Empty fields match all. */
data class RateCardVarianceQuery(
    var country: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardVariance): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
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

    fun apply(items: MutableList<RateCardVariance>): MutableList<RateCardVariance> {
        val matched = filter(items) { candidate: RateCardVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardVariances(store: RateCardVarianceStore, query: RateCardVarianceQuery): MutableList<RateCardVariance> {
    return query.apply(store.list())
}
