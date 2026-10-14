package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel surcharge cycles by code. */
interface FuelSurchargeCycleStore {
    fun find(code: String): FuelSurchargeCycle?
    fun save(item: FuelSurchargeCycle)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelSurchargeCycle>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelSurchargeCycleStore(
    var items: MutableMap<String, FuelSurchargeCycle>,
    var order: MutableList<String>,
) : FuelSurchargeCycleStore {
    override fun find(code: String): FuelSurchargeCycle? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelSurchargeCycle) {
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

    override fun list(): MutableList<FuelSurchargeCycle> {
        val out = mutableListOf<FuelSurchargeCycle>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelSurchargeCycleStore(): FuelSurchargeCycleStore {
    return MemoryFuelSurchargeCycleStore(mutableMapOf<String, FuelSurchargeCycle>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelSurchargeCycleStore(
    var inner: FuelSurchargeCycleStore,
    var cache: TtlCache<FuelSurchargeCycle>,
    var hits: Int,
    var misses: Int,
) : FuelSurchargeCycleStore {
    override fun find(code: String): FuelSurchargeCycle? {
        val hit = (cache.get(code) as FuelSurchargeCycle?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelSurchargeCycle) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelSurchargeCycle> = inner.list()
}

fun newCachedFuelSurchargeCycleStore(inner: FuelSurchargeCycleStore, ttl: Int, clock: Clock): FuelSurchargeCycleStore {
    return CachedFuelSurchargeCycleStore(
        inner = inner,
        cache = newTtlCache<FuelSurchargeCycle>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel surcharge cycles. Empty fields match all. */
data class FuelSurchargeCycleQuery(
    var unit: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelSurchargeCycle): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<FuelSurchargeCycle>): MutableList<FuelSurchargeCycle> {
        val matched = filter(items) { candidate: FuelSurchargeCycle -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelSurchargeCycles(store: FuelSurchargeCycleStore, query: FuelSurchargeCycleQuery): MutableList<FuelSurchargeCycle> {
    return query.apply(store.list())
}
