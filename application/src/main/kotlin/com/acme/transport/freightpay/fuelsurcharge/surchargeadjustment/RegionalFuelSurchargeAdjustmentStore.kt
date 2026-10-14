package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional fuel surcharge adjustments by code. */
interface RegionalFuelSurchargeAdjustmentStore {
    fun find(code: String): RegionalFuelSurchargeAdjustment?
    fun save(item: RegionalFuelSurchargeAdjustment)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalFuelSurchargeAdjustment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalFuelSurchargeAdjustmentStore(
    var items: MutableMap<String, RegionalFuelSurchargeAdjustment>,
    var order: MutableList<String>,
) : RegionalFuelSurchargeAdjustmentStore {
    override fun find(code: String): RegionalFuelSurchargeAdjustment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalFuelSurchargeAdjustment) {
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

    override fun list(): MutableList<RegionalFuelSurchargeAdjustment> {
        val out = mutableListOf<RegionalFuelSurchargeAdjustment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalFuelSurchargeAdjustmentStore(): RegionalFuelSurchargeAdjustmentStore {
    return MemoryRegionalFuelSurchargeAdjustmentStore(
        mutableMapOf<String, RegionalFuelSurchargeAdjustment>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalFuelSurchargeAdjustmentStore(
    var inner: RegionalFuelSurchargeAdjustmentStore,
    var cache: TtlCache<RegionalFuelSurchargeAdjustment>,
    var hits: Int,
    var misses: Int,
) : RegionalFuelSurchargeAdjustmentStore {
    override fun find(code: String): RegionalFuelSurchargeAdjustment? {
        val hit = (cache.get(code) as RegionalFuelSurchargeAdjustment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalFuelSurchargeAdjustment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalFuelSurchargeAdjustment> = inner.list()
}

fun newCachedRegionalFuelSurchargeAdjustmentStore(inner: RegionalFuelSurchargeAdjustmentStore, ttl: Int, clock: Clock): RegionalFuelSurchargeAdjustmentStore {
    return CachedRegionalFuelSurchargeAdjustmentStore(
        inner = inner,
        cache = newTtlCache<RegionalFuelSurchargeAdjustment>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional fuel surcharge adjustments. Empty fields match all. */
data class RegionalFuelSurchargeAdjustmentQuery(
    var depot: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalFuelSurchargeAdjustment): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalFuelSurchargeAdjustment>): MutableList<RegionalFuelSurchargeAdjustment> {
        val matched = filter(items) { candidate: RegionalFuelSurchargeAdjustment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalFuelSurchargeAdjustments(store: RegionalFuelSurchargeAdjustmentStore, query: RegionalFuelSurchargeAdjustmentQuery): MutableList<RegionalFuelSurchargeAdjustment> {
    return query.apply(store.list())
}
