package com.acme.transport.network.transittime.timedigest

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps weighted transit time digests by code. */
interface WeightedTransitTimeDigestStore {
    fun find(code: String): WeightedTransitTimeDigest?
    fun save(item: WeightedTransitTimeDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedTransitTimeDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedTransitTimeDigestStore(
    var items: MutableMap<String, WeightedTransitTimeDigest>,
    var order: MutableList<String>,
) : WeightedTransitTimeDigestStore {
    override fun find(code: String): WeightedTransitTimeDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedTransitTimeDigest) {
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

    override fun list(): MutableList<WeightedTransitTimeDigest> {
        val out = mutableListOf<WeightedTransitTimeDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedTransitTimeDigestStore(): WeightedTransitTimeDigestStore {
    return MemoryWeightedTransitTimeDigestStore(
        mutableMapOf<String, WeightedTransitTimeDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedTransitTimeDigestStore(
    var inner: WeightedTransitTimeDigestStore,
    var cache: TtlCache<WeightedTransitTimeDigest>,
    var hits: Int,
    var misses: Int,
) : WeightedTransitTimeDigestStore {
    override fun find(code: String): WeightedTransitTimeDigest? {
        val hit = (cache.get(code) as WeightedTransitTimeDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedTransitTimeDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedTransitTimeDigest> = inner.list()
}

fun newCachedWeightedTransitTimeDigestStore(inner: WeightedTransitTimeDigestStore, ttl: Int, clock: Clock): WeightedTransitTimeDigestStore {
    return CachedWeightedTransitTimeDigestStore(
        inner = inner,
        cache = newTtlCache<WeightedTransitTimeDigest>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted transit time digests. Empty fields match all. */
data class WeightedTransitTimeDigestQuery(
    var depot: String,
    var minRate: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedTransitTimeDigest): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<WeightedTransitTimeDigest>): MutableList<WeightedTransitTimeDigest> {
        val matched = filter(items) { candidate: WeightedTransitTimeDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedTransitTimeDigests(store: WeightedTransitTimeDigestStore, query: WeightedTransitTimeDigestQuery): MutableList<WeightedTransitTimeDigest> {
    return query.apply(store.list())
}
