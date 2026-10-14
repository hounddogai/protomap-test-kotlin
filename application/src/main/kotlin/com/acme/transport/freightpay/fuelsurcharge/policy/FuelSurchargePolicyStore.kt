package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge policies by code. */
interface FuelSurchargePolicyStore {
    fun find(code: String): FuelSurchargePolicy?
    fun save(item: FuelSurchargePolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargePolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargePolicyStore(
    var items: MutableMap<String, FuelSurchargePolicy>,
    var order: MutableList<String>,
) : FuelSurchargePolicyStore {
    override fun find(code: String): FuelSurchargePolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargePolicy) {
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

    override fun list(): MutableList<FuelSurchargePolicy> {
        val out = mutableListOf<FuelSurchargePolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargePolicyStore(): FuelSurchargePolicyStore {
    return MemoryFuelSurchargePolicyStore(mutableMapOf<String, FuelSurchargePolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargePolicyStore(
    var inner: FuelSurchargePolicyStore,
    var cache: TtlCache<FuelSurchargePolicy>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargePolicyStore {
    override fun find(code: String): FuelSurchargePolicy? {
        val hit = (cache.get(code) as FuelSurchargePolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargePolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargePolicy> = inner.list()
}

fun newCachedFuelSurchargePolicyStore(inner: FuelSurchargePolicyStore, ttl: Int, clock: Clock): FuelSurchargePolicyStore {
    return CachedFuelSurchargePolicyStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargePolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge policies. Empty fields match all. */
data class FuelSurchargePolicyQuery(
    var source: String,
    var minStopCount: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargePolicy): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.stopCount < minStopCount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargePolicy>): MutableList<FuelSurchargePolicy> {
        val matched = filter(items) { candidate: FuelSurchargePolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargePolicies(store: FuelSurchargePolicyStore, query: FuelSurchargePolicyQuery): MutableList<FuelSurchargePolicy> {
    return query.apply(store.list())
}
