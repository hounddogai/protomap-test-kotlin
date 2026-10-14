package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge claims by code. */
interface FuelSurchargeClaimStore {
    fun find(code: String): FuelSurchargeClaim?
    fun save(item: FuelSurchargeClaim)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeClaim>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeClaimStore(
    var items: MutableMap<String, FuelSurchargeClaim>,
    var order: MutableList<String>,
) : FuelSurchargeClaimStore {
    override fun find(code: String): FuelSurchargeClaim? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeClaim) {
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

    override fun list(): MutableList<FuelSurchargeClaim> {
        val out = mutableListOf<FuelSurchargeClaim>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeClaimStore(): FuelSurchargeClaimStore {
    return MemoryFuelSurchargeClaimStore(mutableMapOf<String, FuelSurchargeClaim>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeClaimStore(
    var inner: FuelSurchargeClaimStore,
    var cache: TtlCache<FuelSurchargeClaim>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeClaimStore {
    override fun find(code: String): FuelSurchargeClaim? {
        val hit = (cache.get(code) as FuelSurchargeClaim?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeClaim) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeClaim> = inner.list()
}

fun newCachedFuelSurchargeClaimStore(inner: FuelSurchargeClaimStore, ttl: Int, clock: Clock): FuelSurchargeClaimStore {
    return CachedFuelSurchargeClaimStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeClaim>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge claims. Empty fields match all. */
data class FuelSurchargeClaimQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeClaim): Boolean {
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

    fun apply(items: MutableList<FuelSurchargeClaim>): MutableList<FuelSurchargeClaim> {
        val matched = filter(items) { candidate: FuelSurchargeClaim -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeClaims(store: FuelSurchargeClaimStore, query: FuelSurchargeClaimQuery): MutableList<FuelSurchargeClaim> {
    return query.apply(store.list())
}
