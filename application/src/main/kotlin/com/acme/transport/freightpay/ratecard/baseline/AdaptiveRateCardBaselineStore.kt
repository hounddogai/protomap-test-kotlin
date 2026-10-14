package com.acme.transport.freightpay.ratecard.baseline

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps adaptive rate card baselines by code. */
interface AdaptiveRateCardBaselineStore {
    fun find(code: String): AdaptiveRateCardBaseline?
    fun save(item: AdaptiveRateCardBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveRateCardBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveRateCardBaselineStore(
    var items: MutableMap<String, AdaptiveRateCardBaseline>,
    var order: MutableList<String>,
) : AdaptiveRateCardBaselineStore {
    override fun find(code: String): AdaptiveRateCardBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveRateCardBaseline) {
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

    override fun list(): MutableList<AdaptiveRateCardBaseline> {
        val out = mutableListOf<AdaptiveRateCardBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveRateCardBaselineStore(): AdaptiveRateCardBaselineStore {
    return MemoryAdaptiveRateCardBaselineStore(
        mutableMapOf<String, AdaptiveRateCardBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveRateCardBaselineStore(
    var inner: AdaptiveRateCardBaselineStore,
    var cache: TtlCache<AdaptiveRateCardBaseline>,
    var hits: Int,
    var misses: Int,
) : AdaptiveRateCardBaselineStore {
    override fun find(code: String): AdaptiveRateCardBaseline? {
        val hit = (cache.get(code) as AdaptiveRateCardBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveRateCardBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveRateCardBaseline> = inner.list()
}

fun newCachedAdaptiveRateCardBaselineStore(inner: AdaptiveRateCardBaselineStore, ttl: Int, clock: Clock): AdaptiveRateCardBaselineStore {
    return CachedAdaptiveRateCardBaselineStore(
        inner = inner,
        cache = newTtlCache<AdaptiveRateCardBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive rate card baselines. Empty fields match all. */
data class AdaptiveRateCardBaselineQuery(
    var channel: String,
    var minFuelLiters: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveRateCardBaseline): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.fuelLiters < minFuelLiters) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<AdaptiveRateCardBaseline>): MutableList<AdaptiveRateCardBaseline> {
        val matched = filter(items) { candidate: AdaptiveRateCardBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveRateCardBaselines(store: AdaptiveRateCardBaselineStore, query: AdaptiveRateCardBaselineQuery): MutableList<AdaptiveRateCardBaseline> {
    return query.apply(store.list())
}
