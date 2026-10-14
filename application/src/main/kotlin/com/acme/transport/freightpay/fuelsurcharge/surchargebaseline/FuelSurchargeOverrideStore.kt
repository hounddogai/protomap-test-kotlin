package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge overrides by code. */
interface FuelSurchargeOverrideStore {
    fun find(code: String): FuelSurchargeOverride?
    fun save(item: FuelSurchargeOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeOverrideStore(
    var items: MutableMap<String, FuelSurchargeOverride>,
    var order: MutableList<String>,
) : FuelSurchargeOverrideStore {
    override fun find(code: String): FuelSurchargeOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeOverride) {
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

    override fun list(): MutableList<FuelSurchargeOverride> {
        val out = mutableListOf<FuelSurchargeOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeOverrideStore(): FuelSurchargeOverrideStore {
    return MemoryFuelSurchargeOverrideStore(
        mutableMapOf<String, FuelSurchargeOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeOverrideStore(
    var inner: FuelSurchargeOverrideStore,
    var cache: TtlCache<FuelSurchargeOverride>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeOverrideStore {
    override fun find(code: String): FuelSurchargeOverride? {
        val hit = (cache.get(code) as FuelSurchargeOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeOverride> = inner.list()
}

fun newCachedFuelSurchargeOverrideStore(inner: FuelSurchargeOverrideStore, ttl: Int, clock: Clock): FuelSurchargeOverrideStore {
    return CachedFuelSurchargeOverrideStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeOverride>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge overrides. Empty fields match all. */
data class FuelSurchargeOverrideQuery(
    var segment: String,
    var minStartsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeOverride): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<FuelSurchargeOverride>): MutableList<FuelSurchargeOverride> {
        val matched = filter(items) { candidate: FuelSurchargeOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeOverrides(store: FuelSurchargeOverrideStore, query: FuelSurchargeOverrideQuery): MutableList<FuelSurchargeOverride> {
    return query.apply(store.list())
}
