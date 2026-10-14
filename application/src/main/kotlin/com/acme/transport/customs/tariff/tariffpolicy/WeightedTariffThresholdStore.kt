package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted tariff thresholds by code. */
interface WeightedTariffThresholdStore {
    fun find(code: String): WeightedTariffThreshold?
    fun save(item: WeightedTariffThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTariffThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTariffThresholdStore(
    var items: MutableMap<String, WeightedTariffThreshold>,
    var order: MutableList<String>,
) : WeightedTariffThresholdStore {
    override fun find(code: String): WeightedTariffThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTariffThreshold) {
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

    override fun list(): MutableList<WeightedTariffThreshold> {
        val out = mutableListOf<WeightedTariffThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTariffThresholdStore(): WeightedTariffThresholdStore {
    return MemoryWeightedTariffThresholdStore(
        mutableMapOf<String, WeightedTariffThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTariffThresholdStore(
    var inner: WeightedTariffThresholdStore,
    var cache: TtlCache<WeightedTariffThreshold>,
    var hits: Int,
    var misses: Int,
) : WeightedTariffThresholdStore {
    override fun find(code: String): WeightedTariffThreshold? {
        val hit = (cache.get(code) as WeightedTariffThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTariffThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTariffThreshold> = inner.list()
}

fun newCachedWeightedTariffThresholdStore(inner: WeightedTariffThresholdStore, ttl: Int, clock: Clock): WeightedTariffThresholdStore {
    return CachedWeightedTariffThresholdStore(
        inner = inner,
        cache = newTtlCache<WeightedTariffThreshold>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted tariff thresholds. Empty fields match all. */
data class WeightedTariffThresholdQuery(
    var label: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTariffThreshold): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.startsAt < minStartsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTariffThreshold>): MutableList<WeightedTariffThreshold> {
        val matched = filter(items) { candidate: WeightedTariffThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTariffThresholds(store: WeightedTariffThresholdStore, query: WeightedTariffThresholdQuery): MutableList<WeightedTariffThreshold> {
    return query.apply(store.list())
}
