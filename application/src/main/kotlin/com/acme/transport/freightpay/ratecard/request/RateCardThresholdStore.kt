package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps rate card thresholds by code. */
interface RateCardThresholdStore {
    fun find(code: String): RateCardThreshold?
    fun save(item: RateCardThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<RateCardThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRateCardThresholdStore(
    var items: MutableMap<String, RateCardThreshold>,
    var order: MutableList<String>,
) : RateCardThresholdStore {
    override fun find(code: String): RateCardThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RateCardThreshold) {
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

    override fun list(): MutableList<RateCardThreshold> {
        val out = mutableListOf<RateCardThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRateCardThresholdStore(): RateCardThresholdStore {
    return MemoryRateCardThresholdStore(mutableMapOf<String, RateCardThreshold>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRateCardThresholdStore(
    var inner: RateCardThresholdStore,
    var cache: TtlCache<RateCardThreshold>,
    var hits: Int,
    var misses: Int,
) : RateCardThresholdStore {
    override fun find(code: String): RateCardThreshold? {
        val hit = (cache.get(code) as RateCardThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RateCardThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RateCardThreshold> = inner.list()
}

fun newCachedRateCardThresholdStore(inner: RateCardThresholdStore, ttl: Int, clock: Clock): RateCardThresholdStore {
    return CachedRateCardThresholdStore(
        inner = inner,
        cache = newTtlCache<RateCardThreshold>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rate card thresholds. Empty fields match all. */
data class RateCardThresholdQuery(
    var depot: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RateCardThreshold): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<RateCardThreshold>): MutableList<RateCardThreshold> {
        val matched = filter(items) { candidate: RateCardThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRateCardThresholds(store: RateCardThresholdStore, query: RateCardThresholdQuery): MutableList<RateCardThreshold> {
    return query.apply(store.list())
}
