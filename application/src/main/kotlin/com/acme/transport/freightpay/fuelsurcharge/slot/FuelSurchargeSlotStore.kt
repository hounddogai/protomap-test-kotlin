package com.acme.transport.freightpay.fuelsurcharge.slot

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge slots by code. */
interface FuelSurchargeSlotStore {
    fun find(code: String): FuelSurchargeSlot?
    fun save(item: FuelSurchargeSlot)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeSlot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeSlotStore(
    var items: MutableMap<String, FuelSurchargeSlot>,
    var order: MutableList<String>,
) : FuelSurchargeSlotStore {
    override fun find(code: String): FuelSurchargeSlot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeSlot) {
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

    override fun list(): MutableList<FuelSurchargeSlot> {
        val out = mutableListOf<FuelSurchargeSlot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeSlotStore(): FuelSurchargeSlotStore {
    return MemoryFuelSurchargeSlotStore(mutableMapOf<String, FuelSurchargeSlot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeSlotStore(
    var inner: FuelSurchargeSlotStore,
    var cache: TtlCache<FuelSurchargeSlot>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeSlotStore {
    override fun find(code: String): FuelSurchargeSlot? {
        val hit = (cache.get(code) as FuelSurchargeSlot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeSlot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeSlot> = inner.list()
}

fun newCachedFuelSurchargeSlotStore(inner: FuelSurchargeSlotStore, ttl: Int, clock: Clock): FuelSurchargeSlotStore {
    return CachedFuelSurchargeSlotStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeSlot>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge slots. Empty fields match all. */
data class FuelSurchargeSlotQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeSlot): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeSlot>): MutableList<FuelSurchargeSlot> {
        val matched = filter(items) { candidate: FuelSurchargeSlot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeSlots(store: FuelSurchargeSlotStore, query: FuelSurchargeSlotQuery): MutableList<FuelSurchargeSlot> {
    return query.apply(store.list())
}
