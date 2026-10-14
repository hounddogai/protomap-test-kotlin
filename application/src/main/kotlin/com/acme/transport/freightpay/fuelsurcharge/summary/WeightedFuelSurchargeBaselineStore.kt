package com.acme.transport.freightpay.fuelsurcharge.summary

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps weighted fuel surcharge baselines by code. */
interface WeightedFuelSurchargeBaselineStore {
    fun find(code: String): WeightedFuelSurchargeBaseline?
    fun save(item: WeightedFuelSurchargeBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<WeightedFuelSurchargeBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryWeightedFuelSurchargeBaselineStore(
    var items: MutableMap<String, WeightedFuelSurchargeBaseline>,
    var order: MutableList<String>,
) : WeightedFuelSurchargeBaselineStore {
    override fun find(code: String): WeightedFuelSurchargeBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: WeightedFuelSurchargeBaseline) {
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

    override fun list(): MutableList<WeightedFuelSurchargeBaseline> {
        val out = mutableListOf<WeightedFuelSurchargeBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryWeightedFuelSurchargeBaselineStore(): WeightedFuelSurchargeBaselineStore {
    return MemoryWeightedFuelSurchargeBaselineStore(
        mutableMapOf<String, WeightedFuelSurchargeBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedWeightedFuelSurchargeBaselineStore(
    var inner: WeightedFuelSurchargeBaselineStore,
    var cache: TtlCache<WeightedFuelSurchargeBaseline>,
    var hits: Int,
    var misses: Int,
) : WeightedFuelSurchargeBaselineStore {
    override fun find(code: String): WeightedFuelSurchargeBaseline? {
        val hit = (cache.get(code) as WeightedFuelSurchargeBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: WeightedFuelSurchargeBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<WeightedFuelSurchargeBaseline> = inner.list()
}

fun newCachedWeightedFuelSurchargeBaselineStore(inner: WeightedFuelSurchargeBaselineStore, ttl: Int, clock: Clock): WeightedFuelSurchargeBaselineStore {
    return CachedWeightedFuelSurchargeBaselineStore(
        inner = inner,
        cache = newTtlCache<WeightedFuelSurchargeBaseline>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects weighted fuel surcharge baselines. Empty fields match all. */
data class WeightedFuelSurchargeBaselineQuery(
    var depot: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: WeightedFuelSurchargeBaseline): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<WeightedFuelSurchargeBaseline>): MutableList<WeightedFuelSurchargeBaseline> {
        val matched = filter(items) { candidate: WeightedFuelSurchargeBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findWeightedFuelSurchargeBaselines(store: WeightedFuelSurchargeBaselineStore, query: WeightedFuelSurchargeBaselineQuery): MutableList<WeightedFuelSurchargeBaseline> {
    return query.apply(store.list())
}
