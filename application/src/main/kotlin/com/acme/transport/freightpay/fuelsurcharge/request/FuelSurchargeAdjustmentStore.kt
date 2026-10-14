package com.acme.transport.freightpay.fuelsurcharge.request

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge adjustments by code. */
interface FuelSurchargeAdjustmentStore {
    fun find(code: String): FuelSurchargeAdjustment?
    fun save(item: FuelSurchargeAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeAdjustmentStore(
    var items: MutableMap<String, FuelSurchargeAdjustment>,
    var order: MutableList<String>,
) : FuelSurchargeAdjustmentStore {
    override fun find(code: String): FuelSurchargeAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeAdjustment) {
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

    override fun list(): MutableList<FuelSurchargeAdjustment> {
        val out = mutableListOf<FuelSurchargeAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeAdjustmentStore(): FuelSurchargeAdjustmentStore {
    return MemoryFuelSurchargeAdjustmentStore(
        mutableMapOf<String, FuelSurchargeAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeAdjustmentStore(
    var inner: FuelSurchargeAdjustmentStore,
    var cache: TtlCache<FuelSurchargeAdjustment>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeAdjustmentStore {
    override fun find(code: String): FuelSurchargeAdjustment? {
        val hit = (cache.get(code) as FuelSurchargeAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeAdjustment> = inner.list()
}

fun newCachedFuelSurchargeAdjustmentStore(inner: FuelSurchargeAdjustmentStore, ttl: Int, clock: Clock): FuelSurchargeAdjustmentStore {
    return CachedFuelSurchargeAdjustmentStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge adjustments. Empty fields match all. */
data class FuelSurchargeAdjustmentQuery(
    var name: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeAdjustment): Boolean {
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

    fun apply(items: MutableList<FuelSurchargeAdjustment>): MutableList<FuelSurchargeAdjustment> {
        val matched = filter(items) { candidate: FuelSurchargeAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeAdjustments(store: FuelSurchargeAdjustmentStore, query: FuelSurchargeAdjustmentQuery): MutableList<FuelSurchargeAdjustment> {
    return query.apply(store.list())
}
