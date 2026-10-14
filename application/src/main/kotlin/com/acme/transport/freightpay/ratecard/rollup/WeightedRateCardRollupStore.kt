package com.acme.transport.freightpay.ratecard.rollup

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted rate card rollups by code. */
interface WeightedRateCardRollupStore {
    fun find(code: String): WeightedRateCardRollup?
    fun save(item: WeightedRateCardRollup)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRateCardRollup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRateCardRollupStore(
    var items: MutableMap<String, WeightedRateCardRollup>,
    var order: MutableList<String>,
) : WeightedRateCardRollupStore {
    override fun find(code: String): WeightedRateCardRollup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRateCardRollup) {
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

    override fun list(): MutableList<WeightedRateCardRollup> {
        val out = mutableListOf<WeightedRateCardRollup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRateCardRollupStore(): WeightedRateCardRollupStore {
    return MemoryWeightedRateCardRollupStore(
        mutableMapOf<String, WeightedRateCardRollup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRateCardRollupStore(
    var inner: WeightedRateCardRollupStore,
    var cache: TtlCache<WeightedRateCardRollup>,
    var hits: Int,
    var misses: Int,
) : WeightedRateCardRollupStore {
    override fun find(code: String): WeightedRateCardRollup? {
        val hit = (cache.get(code) as WeightedRateCardRollup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRateCardRollup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRateCardRollup> = inner.list()
}

fun newCachedWeightedRateCardRollupStore(inner: WeightedRateCardRollupStore, ttl: Int, clock: Clock): WeightedRateCardRollupStore {
    return CachedWeightedRateCardRollupStore(
        inner = inner,
        cache = newTtlCache<WeightedRateCardRollup>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted rate card rollups. Empty fields match all. */
data class WeightedRateCardRollupQuery(
    var channel: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRateCardRollup): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedRateCardRollup>): MutableList<WeightedRateCardRollup> {
        val matched = filter(items) { candidate: WeightedRateCardRollup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRateCardRollups(store: WeightedRateCardRollupStore, query: WeightedRateCardRollupQuery): MutableList<WeightedRateCardRollup> {
    return query.apply(store.list())
}
