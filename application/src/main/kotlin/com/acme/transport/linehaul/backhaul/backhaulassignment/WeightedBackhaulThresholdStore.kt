package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted backhaul thresholds by code. */
interface WeightedBackhaulThresholdStore {
    fun find(code: String): WeightedBackhaulThreshold?
    fun save(item: WeightedBackhaulThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedBackhaulThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedBackhaulThresholdStore(
    var items: MutableMap<String, WeightedBackhaulThreshold>,
    var order: MutableList<String>,
) : WeightedBackhaulThresholdStore {
    override fun find(code: String): WeightedBackhaulThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedBackhaulThreshold) {
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

    override fun list(): MutableList<WeightedBackhaulThreshold> {
        val out = mutableListOf<WeightedBackhaulThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedBackhaulThresholdStore(): WeightedBackhaulThresholdStore {
    return MemoryWeightedBackhaulThresholdStore(
        mutableMapOf<String, WeightedBackhaulThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedBackhaulThresholdStore(
    var inner: WeightedBackhaulThresholdStore,
    var cache: TtlCache<WeightedBackhaulThreshold>,
    var hits: Int,
    var misses: Int,
) : WeightedBackhaulThresholdStore {
    override fun find(code: String): WeightedBackhaulThreshold? {
        val hit = (cache.get(code) as WeightedBackhaulThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedBackhaulThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedBackhaulThreshold> = inner.list()
}

fun newCachedWeightedBackhaulThresholdStore(inner: WeightedBackhaulThresholdStore, ttl: Int, clock: Clock): WeightedBackhaulThresholdStore {
    return CachedWeightedBackhaulThresholdStore(
        inner = inner,
        cache = newTtlCache<WeightedBackhaulThreshold>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted backhaul thresholds. Empty fields match all. */
data class WeightedBackhaulThresholdQuery(
    var country: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedBackhaulThreshold): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedBackhaulThreshold>): MutableList<WeightedBackhaulThreshold> {
        val matched = filter(items) { candidate: WeightedBackhaulThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedBackhaulThresholds(store: WeightedBackhaulThresholdStore, query: WeightedBackhaulThresholdQuery): MutableList<WeightedBackhaulThreshold> {
    return query.apply(store.list())
}
