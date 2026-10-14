package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card estimates by code. */
interface RateCardEstimateStore {
    fun find(code: String): RateCardEstimate?
    fun save(item: RateCardEstimate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardEstimate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardEstimateStore(
    var items: MutableMap<String, RateCardEstimate>,
    var order: MutableList<String>,
) : RateCardEstimateStore {
    override fun find(code: String): RateCardEstimate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardEstimate) {
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

    override fun list(): MutableList<RateCardEstimate> {
        val out = mutableListOf<RateCardEstimate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardEstimateStore(): RateCardEstimateStore {
    return MemoryRateCardEstimateStore(mutableMapOf<String, RateCardEstimate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardEstimateStore(
    var inner: RateCardEstimateStore,
    var cache: TtlCache<RateCardEstimate>,
    var hits: Int,
    var misses: Int,
) : RateCardEstimateStore {
    override fun find(code: String): RateCardEstimate? {
        val hit = (cache.get(code) as RateCardEstimate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardEstimate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardEstimate> = inner.list()
}

fun newCachedRateCardEstimateStore(inner: RateCardEstimateStore, ttl: Int, clock: Clock): RateCardEstimateStore {
    return CachedRateCardEstimateStore(
        inner = inner,
        cache = newTtlCache<RateCardEstimate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card estimates. Empty fields match all. */
data class RateCardEstimateQuery(
    var segment: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardEstimate): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RateCardEstimate>): MutableList<RateCardEstimate> {
        val matched = filter(items) { candidate: RateCardEstimate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardEstimates(store: RateCardEstimateStore, query: RateCardEstimateQuery): MutableList<RateCardEstimate> {
    return query.apply(store.list())
}
