package com.acme.transport.freightpay.ratecard.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted rate card thresholds by code. */
interface WeightedRateCardThresholdStore {
    fun find(code: String): WeightedRateCardThreshold?
    fun save(item: WeightedRateCardThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRateCardThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRateCardThresholdStore(
    var items: MutableMap<String, WeightedRateCardThreshold>,
    var order: MutableList<String>,
) : WeightedRateCardThresholdStore {
    override fun find(code: String): WeightedRateCardThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRateCardThreshold) {
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

    override fun list(): MutableList<WeightedRateCardThreshold> {
        val out = mutableListOf<WeightedRateCardThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRateCardThresholdStore(): WeightedRateCardThresholdStore {
    return MemoryWeightedRateCardThresholdStore(
        mutableMapOf<String, WeightedRateCardThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRateCardThresholdStore(
    var inner: WeightedRateCardThresholdStore,
    var cache: TtlCache<WeightedRateCardThreshold>,
    var hits: Int,
    var misses: Int,
) : WeightedRateCardThresholdStore {
    override fun find(code: String): WeightedRateCardThreshold? {
        val hit = (cache.get(code) as WeightedRateCardThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRateCardThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRateCardThreshold> = inner.list()
}

fun newCachedWeightedRateCardThresholdStore(inner: WeightedRateCardThresholdStore, ttl: Int, clock: Clock): WeightedRateCardThresholdStore {
    return CachedWeightedRateCardThresholdStore(
        inner = inner,
        cache = newTtlCache<WeightedRateCardThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted rate card thresholds. Empty fields match all. */
data class WeightedRateCardThresholdQuery(
    var name: String,
    var minVersion: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRateCardThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.version < minVersion) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedRateCardThreshold>): MutableList<WeightedRateCardThreshold> {
        val matched = filter(items) { candidate: WeightedRateCardThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRateCardThresholds(store: WeightedRateCardThresholdStore, query: WeightedRateCardThresholdQuery): MutableList<WeightedRateCardThreshold> {
    return query.apply(store.list())
}
