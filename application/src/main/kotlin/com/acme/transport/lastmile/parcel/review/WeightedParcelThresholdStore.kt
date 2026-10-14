package com.acme.transport.lastmile.parcel.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps weighted parcel thresholds by code. */
interface WeightedParcelThresholdStore {
    fun find(code: String): WeightedParcelThreshold?
    fun save(item: WeightedParcelThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedParcelThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedParcelThresholdStore(
    var items: MutableMap<String, WeightedParcelThreshold>,
    var order: MutableList<String>,
) : WeightedParcelThresholdStore {
    override fun find(code: String): WeightedParcelThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedParcelThreshold) {
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

    override fun list(): MutableList<WeightedParcelThreshold> {
        val out = mutableListOf<WeightedParcelThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedParcelThresholdStore(): WeightedParcelThresholdStore {
    return MemoryWeightedParcelThresholdStore(
        mutableMapOf<String, WeightedParcelThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedParcelThresholdStore(
    var inner: WeightedParcelThresholdStore,
    var cache: TtlCache<WeightedParcelThreshold>,
    var hits: Int,
    var misses: Int,
) : WeightedParcelThresholdStore {
    override fun find(code: String): WeightedParcelThreshold? {
        val hit = (cache.get(code) as WeightedParcelThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedParcelThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedParcelThreshold> = inner.list()
}

fun newCachedWeightedParcelThresholdStore(inner: WeightedParcelThresholdStore, ttl: Int, clock: Clock): WeightedParcelThresholdStore {
    return CachedWeightedParcelThresholdStore(
        inner = inner,
        cache = newTtlCache<WeightedParcelThreshold>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted parcel thresholds. Empty fields match all. */
data class WeightedParcelThresholdQuery(
    var title: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedParcelThreshold): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.rate < minRate) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedParcelThreshold>): MutableList<WeightedParcelThreshold> {
        val matched = filter(items) { candidate: WeightedParcelThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedParcelThresholds(store: WeightedParcelThresholdStore, query: WeightedParcelThresholdQuery): MutableList<WeightedParcelThreshold> {
    return query.apply(store.list())
}
