package com.acme.transport.freightpay.ratecard.cardsegment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted rate card digests by code. */
interface WeightedRateCardDigestStore {
    fun find(code: String): WeightedRateCardDigest?
    fun save(item: WeightedRateCardDigest)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedRateCardDigest>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedRateCardDigestStore(
    var items: MutableMap<String, WeightedRateCardDigest>,
    var order: MutableList<String>,
) : WeightedRateCardDigestStore {
    override fun find(code: String): WeightedRateCardDigest? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedRateCardDigest) {
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

    override fun list(): MutableList<WeightedRateCardDigest> {
        val out = mutableListOf<WeightedRateCardDigest>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedRateCardDigestStore(): WeightedRateCardDigestStore {
    return MemoryWeightedRateCardDigestStore(
        mutableMapOf<String, WeightedRateCardDigest>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedRateCardDigestStore(
    var inner: WeightedRateCardDigestStore,
    var cache: TtlCache<WeightedRateCardDigest>,
    var hits: Int,
    var misses: Int,
) : WeightedRateCardDigestStore {
    override fun find(code: String): WeightedRateCardDigest? {
        val hit = (cache.get(code) as WeightedRateCardDigest?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedRateCardDigest) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedRateCardDigest> = inner.list()
}

fun newCachedWeightedRateCardDigestStore(inner: WeightedRateCardDigestStore, ttl: Int, clock: Clock): WeightedRateCardDigestStore {
    return CachedWeightedRateCardDigestStore(
        inner = inner,
        cache = newTtlCache<WeightedRateCardDigest>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted rate card digests. Empty fields match all. */
data class WeightedRateCardDigestQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedRateCardDigest): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<WeightedRateCardDigest>): MutableList<WeightedRateCardDigest> {
        val matched = filter(items) { candidate: WeightedRateCardDigest -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedRateCardDigests(store: WeightedRateCardDigestStore, query: WeightedRateCardDigestQuery): MutableList<WeightedRateCardDigest> {
    return query.apply(store.list())
}
