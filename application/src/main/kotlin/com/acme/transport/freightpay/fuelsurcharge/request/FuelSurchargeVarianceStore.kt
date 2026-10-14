package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge variances by code. */
interface FuelSurchargeVarianceStore {
    fun find(code: String): FuelSurchargeVariance?
    fun save(item: FuelSurchargeVariance)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeVariance>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeVarianceStore(
    var items: MutableMap<String, FuelSurchargeVariance>,
    var order: MutableList<String>,
) : FuelSurchargeVarianceStore {
    override fun find(code: String): FuelSurchargeVariance? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeVariance) {
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

    override fun list(): MutableList<FuelSurchargeVariance> {
        val out = mutableListOf<FuelSurchargeVariance>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeVarianceStore(): FuelSurchargeVarianceStore {
    return MemoryFuelSurchargeVarianceStore(
        mutableMapOf<String, FuelSurchargeVariance>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeVarianceStore(
    var inner: FuelSurchargeVarianceStore,
    var cache: TtlCache<FuelSurchargeVariance>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeVarianceStore {
    override fun find(code: String): FuelSurchargeVariance? {
        val hit = (cache.get(code) as FuelSurchargeVariance?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeVariance) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeVariance> = inner.list()
}

fun newCachedFuelSurchargeVarianceStore(inner: FuelSurchargeVarianceStore, ttl: Int, clock: Clock): FuelSurchargeVarianceStore {
    return CachedFuelSurchargeVarianceStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeVariance>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge variances. Empty fields match all. */
data class FuelSurchargeVarianceQuery(
    var source: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeVariance): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
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

    fun apply(items: MutableList<FuelSurchargeVariance>): MutableList<FuelSurchargeVariance> {
        val matched = filter(items) { candidate: FuelSurchargeVariance -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeVariances(store: FuelSurchargeVarianceStore, query: FuelSurchargeVarianceQuery): MutableList<FuelSurchargeVariance> {
    return query.apply(store.list())
}
