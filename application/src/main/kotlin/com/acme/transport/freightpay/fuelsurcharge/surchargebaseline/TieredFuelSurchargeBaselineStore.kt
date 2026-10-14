package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered fuel surcharge baselines by code. */
interface TieredFuelSurchargeBaselineStore {
    fun find(code: String): TieredFuelSurchargeBaseline?
    fun save(item: TieredFuelSurchargeBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredFuelSurchargeBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredFuelSurchargeBaselineStore(
    var items: MutableMap<String, TieredFuelSurchargeBaseline>,
    var order: MutableList<String>,
) : TieredFuelSurchargeBaselineStore {
    override fun find(code: String): TieredFuelSurchargeBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredFuelSurchargeBaseline) {
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

    override fun list(): MutableList<TieredFuelSurchargeBaseline> {
        val out = mutableListOf<TieredFuelSurchargeBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredFuelSurchargeBaselineStore(): TieredFuelSurchargeBaselineStore {
    return MemoryTieredFuelSurchargeBaselineStore(
        mutableMapOf<String, TieredFuelSurchargeBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredFuelSurchargeBaselineStore(
    var inner: TieredFuelSurchargeBaselineStore,
    var cache: TtlCache<TieredFuelSurchargeBaseline>,
    var hits: Int,
    var misses: Int,
) : TieredFuelSurchargeBaselineStore {
    override fun find(code: String): TieredFuelSurchargeBaseline? {
        val hit = (cache.get(code) as TieredFuelSurchargeBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredFuelSurchargeBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredFuelSurchargeBaseline> = inner.list()
}

fun newCachedTieredFuelSurchargeBaselineStore(inner: TieredFuelSurchargeBaselineStore, ttl: Int, clock: Clock): TieredFuelSurchargeBaselineStore {
    return CachedTieredFuelSurchargeBaselineStore(
        inner = inner,
        cache = newTtlCache<TieredFuelSurchargeBaseline>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered fuel surcharge baselines. Empty fields match all. */
data class TieredFuelSurchargeBaselineQuery(
    var unit: String,
    var minDistanceKm: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredFuelSurchargeBaseline): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.distanceKm < minDistanceKm) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredFuelSurchargeBaseline>): MutableList<TieredFuelSurchargeBaseline> {
        val matched = filter(items) { candidate: TieredFuelSurchargeBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredFuelSurchargeBaselines(store: TieredFuelSurchargeBaselineStore, query: TieredFuelSurchargeBaselineQuery): MutableList<TieredFuelSurchargeBaseline> {
    return query.apply(store.list())
}
