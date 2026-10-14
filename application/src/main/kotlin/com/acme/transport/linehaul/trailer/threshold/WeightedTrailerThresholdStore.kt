package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps weighted trailer thresholds by code. */
interface WeightedTrailerThresholdStore {
    fun find(code: String): WeightedTrailerThreshold?
    fun save(item: WeightedTrailerThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTrailerThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTrailerThresholdStore(
    var items: MutableMap<String, WeightedTrailerThreshold>,
    var order: MutableList<String>,
) : WeightedTrailerThresholdStore {
    override fun find(code: String): WeightedTrailerThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTrailerThreshold) {
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

    override fun list(): MutableList<WeightedTrailerThreshold> {
        val out = mutableListOf<WeightedTrailerThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTrailerThresholdStore(): WeightedTrailerThresholdStore {
    return MemoryWeightedTrailerThresholdStore(
        mutableMapOf<String, WeightedTrailerThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTrailerThresholdStore(
    var inner: WeightedTrailerThresholdStore,
    var cache: TtlCache<WeightedTrailerThreshold>,
    var hits: Int,
    var misses: Int,
) : WeightedTrailerThresholdStore {
    override fun find(code: String): WeightedTrailerThreshold? {
        val hit = (cache.get(code) as WeightedTrailerThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTrailerThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTrailerThreshold> = inner.list()
}

fun newCachedWeightedTrailerThresholdStore(inner: WeightedTrailerThresholdStore, ttl: Int, clock: Clock): WeightedTrailerThresholdStore {
    return CachedWeightedTrailerThresholdStore(
        inner = inner,
        cache = newTtlCache<WeightedTrailerThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted trailer thresholds. Empty fields match all. */
data class WeightedTrailerThresholdQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTrailerThreshold): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedTrailerThreshold>): MutableList<WeightedTrailerThreshold> {
        val matched = filter(items) { candidate: WeightedTrailerThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTrailerThresholds(store: WeightedTrailerThresholdStore, query: WeightedTrailerThresholdQuery): MutableList<WeightedTrailerThreshold> {
    return query.apply(store.list())
}
