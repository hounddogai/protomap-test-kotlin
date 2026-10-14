package com.acme.transport.freightpay.ratecard.threshold

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict rate card thresholds by code. */
interface StrictRateCardThresholdStore {
    fun find(code: String): StrictRateCardThreshold?
    fun save(item: StrictRateCardThreshold)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictRateCardThreshold>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictRateCardThresholdStore(
    var items: MutableMap<String, StrictRateCardThreshold>,
    var order: MutableList<String>,
) : StrictRateCardThresholdStore {
    override fun find(code: String): StrictRateCardThreshold? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictRateCardThreshold) {
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

    override fun list(): MutableList<StrictRateCardThreshold> {
        val out = mutableListOf<StrictRateCardThreshold>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictRateCardThresholdStore(): StrictRateCardThresholdStore {
    return MemoryStrictRateCardThresholdStore(
        mutableMapOf<String, StrictRateCardThreshold>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictRateCardThresholdStore(
    var inner: StrictRateCardThresholdStore,
    var cache: TtlCache<StrictRateCardThreshold>,
    var hits: Int,
    var misses: Int,
) : StrictRateCardThresholdStore {
    override fun find(code: String): StrictRateCardThreshold? {
        val hit = (cache.get(code) as StrictRateCardThreshold?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictRateCardThreshold) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictRateCardThreshold> = inner.list()
}

fun newCachedStrictRateCardThresholdStore(inner: StrictRateCardThresholdStore, ttl: Int, clock: Clock): StrictRateCardThresholdStore {
    return CachedStrictRateCardThresholdStore(
        inner = inner,
        cache = newTtlCache<StrictRateCardThreshold>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict rate card thresholds. Empty fields match all. */
data class StrictRateCardThresholdQuery(
    var depot: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictRateCardThreshold): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictRateCardThreshold>): MutableList<StrictRateCardThreshold> {
        val matched = filter(items) { candidate: StrictRateCardThreshold -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictRateCardThresholds(store: StrictRateCardThresholdStore, query: StrictRateCardThresholdQuery): MutableList<StrictRateCardThreshold> {
    return query.apply(store.list())
}
