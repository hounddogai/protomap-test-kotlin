package com.acme.transport.crewing.availability.ledger

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted availability digests by code. */
interface WeightedAvailabilityDigestStore {
    fun find(code: String): WeightedAvailabilityDigest?
    fun save(item: WeightedAvailabilityDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedAvailabilityDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedAvailabilityDigestStore(
    var items: MutableMap<String, WeightedAvailabilityDigest>,
    var order: MutableList<String>,
) : WeightedAvailabilityDigestStore {
    override fun find(code: String): WeightedAvailabilityDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedAvailabilityDigest) {
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

    override fun list(): MutableList<WeightedAvailabilityDigest> {
        val out = mutableListOf<WeightedAvailabilityDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedAvailabilityDigestStore(): WeightedAvailabilityDigestStore {
    return MemoryWeightedAvailabilityDigestStore(
        mutableMapOf<String, WeightedAvailabilityDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedAvailabilityDigestStore(
    var inner: WeightedAvailabilityDigestStore,
    var cache: TtlCache<WeightedAvailabilityDigest>,
    var hits: Int,
    var misses: Int,
) : WeightedAvailabilityDigestStore {
    override fun find(code: String): WeightedAvailabilityDigest? {
        val hit = (cache.get(code) as WeightedAvailabilityDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedAvailabilityDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedAvailabilityDigest> = inner.list()
}

fun newCachedWeightedAvailabilityDigestStore(inner: WeightedAvailabilityDigestStore, ttl: Int, clock: Clock): WeightedAvailabilityDigestStore {
    return CachedWeightedAvailabilityDigestStore(
        inner = inner,
        cache = newTtlCache<WeightedAvailabilityDigest>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted availability digests. Empty fields match all. */
data class WeightedAvailabilityDigestQuery(
    var depot: String,
    var minMaximum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedAvailabilityDigest): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.maximum < minMaximum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedAvailabilityDigest>): MutableList<WeightedAvailabilityDigest> {
        val matched = filter(items) { candidate: WeightedAvailabilityDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedAvailabilityDigests(store: WeightedAvailabilityDigestStore, query: WeightedAvailabilityDigestQuery): MutableList<WeightedAvailabilityDigest> {
    return query.apply(store.list())
}
