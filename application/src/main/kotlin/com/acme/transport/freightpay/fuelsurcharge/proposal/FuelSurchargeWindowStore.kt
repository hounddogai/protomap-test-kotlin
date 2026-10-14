package com.acme.transport.freightpay.fuelsurcharge.proposal

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge windows by code. */
interface FuelSurchargeWindowStore {
    fun find(code: String): FuelSurchargeWindow?
    fun save(item: FuelSurchargeWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeWindowStore(
    var items: MutableMap<String, FuelSurchargeWindow>,
    var order: MutableList<String>,
) : FuelSurchargeWindowStore {
    override fun find(code: String): FuelSurchargeWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeWindow) {
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

    override fun list(): MutableList<FuelSurchargeWindow> {
        val out = mutableListOf<FuelSurchargeWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeWindowStore(): FuelSurchargeWindowStore {
    return MemoryFuelSurchargeWindowStore(mutableMapOf<String, FuelSurchargeWindow>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeWindowStore(
    var inner: FuelSurchargeWindowStore,
    var cache: TtlCache<FuelSurchargeWindow>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeWindowStore {
    override fun find(code: String): FuelSurchargeWindow? {
        val hit = (cache.get(code) as FuelSurchargeWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeWindow> = inner.list()
}

fun newCachedFuelSurchargeWindowStore(inner: FuelSurchargeWindowStore, ttl: Int, clock: Clock): FuelSurchargeWindowStore {
    return CachedFuelSurchargeWindowStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeWindow>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge windows. Empty fields match all. */
data class FuelSurchargeWindowQuery(
    var country: String,
    var minThreshold: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeWindow): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.threshold < minThreshold) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeWindow>): MutableList<FuelSurchargeWindow> {
        val matched = filter(items) { candidate: FuelSurchargeWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeWindows(store: FuelSurchargeWindowStore, query: FuelSurchargeWindowQuery): MutableList<FuelSurchargeWindow> {
    return query.apply(store.list())
}
